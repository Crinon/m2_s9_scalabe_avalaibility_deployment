package org.miage.bankservice.boundary;

import org.miage.bankservice.assembler.CardAssembler;
import org.miage.bankservice.assembler.TransfertAssembler;
import org.miage.bankservice.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.ReflectionUtils;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.validation.Valid;

@RestController
@RequestMapping(value = "/transferts", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Transfert.class)
public class TransfertRepresentation {
    private final TransfertResource transfertResource;
    private final CardResource cardResource;
    private final AccountResource accountResource;
    private final TransfertAssembler transfertAssembler;
    private final TransfertValidator transfertValidator;
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRepresentation.class);

    public TransfertRepresentation(TransfertResource transfertResource,
                                   TransfertAssembler transfertAssembler,
                                   TransfertValidator transfertValidator,
                                   AccountResource accountResource,
                                   CardResource cardResource) {
        this.transfertResource = transfertResource;
        this.transfertAssembler = transfertAssembler;
        this.transfertValidator = transfertValidator;
        this.accountResource = accountResource;
        this.cardResource = cardResource;
    }

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllTransferts() {
        return ResponseEntity.ok(transfertAssembler.toCollectionModel(transfertResource.findAll()));
    }

    // GET one
    @GetMapping(value = "/{transfertId}")
    public ResponseEntity<?> getOneTransfert(@PathVariable("transfertId") String id) {
        return Optional.ofNullable(transfertResource.findById(id)).filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(transfertAssembler.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    @Transactional
    public ResponseEntity<?> saveTransfert(@RequestBody @Valid TransfertInput transfertinput) {
//      Un transfer se fait entre deux comptes, on vérifie que les comptes spécifiés existent bien
        Optional<Account> optionalAccountFrom = accountResource.findById(transfertinput.getIdaccountFrom());
        Optional<Account> optionalAccountTo = accountResource.findById(transfertinput.getIdaccountTo());
        if (!optionalAccountFrom.isPresent()) {
            // L'utilisateur a tenté de PATCH son solde bancaire
            String errorMessage = "{\"message\":\"Request not processed, reason is : Sender account not found\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }
        if (!optionalAccountTo.isPresent()) {
            // L'utilisateur a tenté de PATCH son solde bancaire
            String errorMessage = "{\"message\":\"Request not processed, reason is : Reciever account not found\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

//      Si tout est OK alors on débite l'expéditeur
        Card debitedCard = cardResource.findById(optionalAccountFrom.get().getFkidcard()).get();
        Double moneyBeforeDebit = Double.parseDouble(debitedCard.getCash());
        Double moneyAfterDebit = moneyBeforeDebit - transfertinput.getAmount();
        debitedCard.setCash(String.valueOf(moneyAfterDebit));
        cardResource.save(debitedCard);

        //      Si tout est OK alors on crédite le destinataire
        Card creditedCard = cardResource.findById(optionalAccountTo.get().getFkidcard()).get();
        Double moneyBeforeCredit = Double.parseDouble(creditedCard.getCash());
        Double moneyAfterCredit = moneyBeforeCredit + transfertinput.getAmount();
        creditedCard.setCash(String.valueOf(moneyAfterCredit));
        cardResource.save(creditedCard);



//      Si tout est OK alors on procède au transfert
        Transfert tranfert2Save = new Transfert(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                transfertinput.getAmount(),
                optionalAccountFrom.get(),
                optionalAccountTo.get()
        );
        Transfert savedTransfert = transfertResource.save(tranfert2Save);

        URI location = linkTo(TransfertRepresentation.class).slash(savedTransfert.getIdtransfert()).toUri();
        return ResponseEntity.created(location).build();
    }

}
