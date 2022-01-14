package org.miage.bankservice.boundary;

import lombok.RequiredArgsConstructor;
import org.miage.bankservice.assembler.OperationAssembler;
import org.miage.bankservice.entity.*;
import org.miage.bankservice.miscellaneous.CustomErrorHandler;
import org.miage.bankservice.miscellaneous.Exchangerate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.restassured.response.Response;

import javax.annotation.security.RolesAllowed;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/operations", produces = MediaType.APPLICATION_JSON_VALUE)
@RolesAllowed("ROLE_USER")
@RequiredArgsConstructor
@ExposesResourceFor(Operation.class)
public class OperationRepresentation extends CustomErrorHandler {

    private static final Logger LOGGER=LoggerFactory.getLogger(OperationRepresentation.class);
    private final OperationResource operationResource;
    private final OperationAssembler operationAssembler;
    private final AccountResource accountResource;
    private final CardResource cardResource;

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllOperationsFromConnectedUser() {
        // ID du compte connecté
        String connectedAccountId = accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get().getId();
        // Ensemble des opérations dans lesquelles figure le compte connecté
        List<Operation> operations = operationResource.findByAccountFrom_IdEqualsIgnoreCaseOrAccountTo_IdEqualsIgnoreCase(connectedAccountId,connectedAccountId);
        return ResponseEntity.ok(operationAssembler.toCollectionModel(operations));
    }

    // GET one
    @GetMapping(value="/{operationId}")
    public ResponseEntity<?> getOneOperation(@PathVariable("operationId") String id) {
        // ID du compte connecté
        String connectedAccountId = accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get().getId();
        Optional <Operation> operation = operationResource.findById(id);
        if(operation.isPresent()){
            if(operation.get().getAccountFrom().getId() == connectedAccountId ||operation.get().getAccountTo().getId() == connectedAccountId ){
                return ResponseEntity.ok(operationAssembler.toModel(operation.get()));
            }
            String errorMessage = "{\"message\":\"Request not processed, reason is : operation searched is not related to account connected\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> saveOperation(@RequestBody @Valid OperationInput operationInput)  {
        // L'opération doit concerner le compte connecté
        String connectedAccountId = accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get().getId();
        if(!(connectedAccountId.equals(operationInput.getIdaccountCustomer()) || connectedAccountId.equals(operationInput.getIdaccountShop()))){
            String errorMessage = "{\"message\":\"Request not processed, reason is : operation must be related to accountid connected\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

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

        // Une transaction ne peut se faire si le customer a bloqué sa carte
        Card customerCard = cardResource.findById(optionalAccountFrom.get().getFkidcard()).get();
        if (customerCard.isBlocked()) {
            // L'utilisateur n'utilise un id de compte (shop) qui n'existe pas
            String errorMessage = "{\"message\":\"Request not processed, reason is : Customer's card is blocked\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // On ne réalise pas la transaction si :
        //      le customer utilise la sécurité GPS
        //      le client et le shop ne sont pas dans le même pays
        if (customerCard.isRegionLocked() && (optionalAccountTo.get().getCountry() != optionalAccountFrom.get().getCountry())) {
            // L'utilisateur n'utilise un id de compte (shop) qui n'existe pas
            String errorMessage = "{\"message\":\"Request not processed, reason is : region lock is enable and countries are different\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // On arrête si le customer dépasse la slidinglimit avec cette transaction
        if (operationInput.getAmount() > customerCard.getSlidinglimit()) {
            // L'utilisateur n'utilise un id de compte (shop) qui n'existe pas
            String errorMessage = "{\"message\":\"Request not processed, reason is : the amount of the transaction exceeds the limit. \"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

//      Si tout est OK alors on débite le client
        Double moneyBeforeDebit = Double.parseDouble(customerCard.getCash());
        Double priceInEuro = operationInput.getAmount()*Exchangerate.exchangeRate.get(optionalAccountTo.get().getCountry());
        Double moneyAfterDebit = moneyBeforeDebit - priceInEuro;
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
                Exchangerate.exchangeRate.get(optionalAccountTo.get().getCountry()),
                recipientName,
                operationInput.getCategory(),
                countryShop,
                optionalAccountFrom.get(),
                optionalAccountTo.get()
        );
        Operation operationSaved =operationResource.save(operation2Save);
        URI location = linkTo(OperationRepresentation.class).slash(operationSaved.getId()).toUri();
        return ResponseEntity.created(location).build();
    }


    // GET proceed shopservice operation
    @GetMapping("/shopservice/shopid/{shopid}/customerid/{customerid}/amount/{amount}")
    public ResponseEntity<?> proceedShopserviceOperation(@PathVariable("shopid") String shopid,
                                                         @PathVariable("customerid") String customerid,
                                                         @PathVariable Double amount) {
        OperationInput operationInput = new OperationInput();
        operationInput.setAmount(amount);
        operationInput.setIdaccountShop(shopid);
        operationInput.setIdaccountCustomer(customerid);
        // On essaie de créer l'opération
        ResponseEntity response = saveOperation(operationInput);
        return response;
    }

    // GET proceed shopservice operation
    @GetMapping("/shopservice/shopid/{shopid}/customercardnumber/{customercardnumber}/amount/{amount}")
    public ResponseEntity<?> proceedDirectShopserviceOperation(@PathVariable("shopid") String shopid,
                                                               @PathVariable("customercardnumber") String customercardnumber,
                                                               @PathVariable Double amount) {
        // A partir du numéro de carte du client on trouve son accountid
        Optional<Card> customercard = cardResource.findByNumberEqualsIgnoreCase(customercardnumber);
        if (!customercard.isPresent()) {
            String errorMessage = "{\"message\":\"Request not processed, reason is : card number does not exist\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }
        Account accountCustomer = accountResource.findByFkidcardEqualsIgnoreCase(customercard.get().getIdcard());
        OperationInput operationInput = new OperationInput();
        operationInput.setAmount(amount);
        operationInput.setIdaccountShop(shopid);
        operationInput.setIdaccountCustomer(accountCustomer.getId());
        // On essaie de créer l'opération
        ResponseEntity response = saveOperation(operationInput);
        return response;
    }

}
