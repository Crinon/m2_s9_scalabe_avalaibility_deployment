package org.miage.bankservice.boundary;

import org.miage.bankservice.assembler.OperationAssembler;
import org.miage.bankservice.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/operations", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Operation.class)
public class OperationRepresentation {

    private static final Logger LOGGER=LoggerFactory.getLogger(OperationRepresentation.class);

    private final OperationResource operationResource;
    private final OperationAssembler operationAssembler;
    private final AccountResource accountResource;
    private final CardResource cardResource;


    public OperationRepresentation(OperationResource operationResource,
                                   OperationAssembler operationAssembler,
                                   AccountResource accountResource,
                                   CardResource cardResource) {
        this.operationResource = operationResource;
        this.operationAssembler = operationAssembler;
        this.accountResource = accountResource;
        this.cardResource = cardResource;
    }

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllOperation() {
        return ResponseEntity.ok(operationAssembler.toCollectionModel(operationResource.findAll()));
    }

    // GET one
    @GetMapping(value="/{operationId}")
    public ResponseEntity<?> getOneOperation(@PathVariable("operationId") String id) {
        return Optional.ofNullable(operationResource.findById(id)).filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(operationAssembler.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> saveOperation(@RequestBody @Valid OperationInput operationInput)  {
//      Une opération se fait entre deux comptes, on vérifie que les comptes spécifiés existent bien
        Optional<Account> optionalAccountFrom = accountResource.findById(operationInput.getIdaccountCustomer());
        Optional<Account> optionalAccountTo = accountResource.findById(operationInput.getIdaccountShop());
        if (!optionalAccountFrom.isPresent()) {
            // L'utilisateur n'utilise pas son id de compte
            String errorMessage = "{\"message\":\"Request not processed, reason is : Customer account not found\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }
        if (!optionalAccountTo.isPresent()) {
            // L'utilisateur n'utilise un id de compte (shop) qui n'existe pas
            String errorMessage = "{\"message\":\"Request not processed, reason is : Shopper account not found\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

//      Si tout est OK alors on débite le client
        Card customerCard = cardResource.findById(optionalAccountFrom.get().getFkidcard()).get();
        Double moneyBeforeDebit = Double.parseDouble(customerCard.getCash());
        Double moneyAfterDebit = moneyBeforeDebit - operationInput.getAmount();
        customerCard.setCash(String.valueOf(moneyAfterDebit));
        cardResource.save(customerCard);

        //      Si tout est OK alors on crédite le shop
        Card shopCard = cardResource.findById(optionalAccountTo.get().getFkidcard()).get();
        Double moneyBeforeCredit = Double.parseDouble(shopCard.getCash());
        Double moneyAfterCredit = moneyBeforeCredit + operationInput.getAmount();
        shopCard.setCash(String.valueOf(moneyAfterCredit));
        cardResource.save(shopCard);

        // On récupère le nom du compte destinaire
        String recipientName = optionalAccountTo.get().getName() + " " + optionalAccountTo.get().getSurname();
        // On récupère le pays du shop pour connaitre le taux de conversion
        Account.Country countryShop = optionalAccountTo.get().getCountry();

//      Si tout est OK alors on procède au transfert
        Operation operation2Save = new Operation(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                operationInput.getWording(),
                operationInput.getAmount(),
                operationInput.getConversionRate(),
                recipientName,
                operationInput.getCategory(),
                countryShop,
                optionalAccountFrom.get(),
                optionalAccountTo.get()
        );
        Operation operationSaved =operationResource.save(operation2Save);
        URI location = linkTo(AccountRepresentation.class).slash(operationSaved.getId()).toUri();
        return ResponseEntity.created(location).build();
    }
}
