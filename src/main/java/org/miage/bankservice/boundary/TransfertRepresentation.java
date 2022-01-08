package org.miage.bankservice.boundary;

import org.miage.bankservice.assembler.TransfertAssembler;
import org.miage.bankservice.entity.*;
import org.miage.bankservice.miscellaneous.Exchangerate;
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
@RequestMapping(value = "/transferts", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Transfert.class)
public class TransfertRepresentation {
    private final TransfertResource transfertResource;
    private final CardResource cardResource;
    private final AccountResource accountResource;
    private final TransfertAssembler transfertAssembler;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransfertRepresentation.class);

    public TransfertRepresentation(TransfertResource transfertResource,
                                   TransfertAssembler transfertAssembler,
                                   AccountResource accountResource,
                                   CardResource cardResource) {
        this.transfertResource = transfertResource;
        this.transfertAssembler = transfertAssembler;
        this.accountResource = accountResource;
        this.cardResource = cardResource;
    }

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllTransferts() {
        return ResponseEntity.ok(transfertAssembler.toCollectionModel(transfertResource.findAll()));
    }

    // GET all transfert from account
    @GetMapping(value = "/account/{accountId}")
    public ResponseEntity<?> getAllTransfertByAccount(@PathVariable("accountId") String id) {
        return ResponseEntity.ok(transfertAssembler.toCollectionModel(transfertResource.findByAccountFrom_IdEqualsIgnoreCaseOrAccountTo_IdEqualsIgnoreCase(id,id)));
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
            // L'utilisateur n'utilise pas son id de compte
            String errorMessage = "{\"message\":\"Request not processed, reason is : Sender account not found\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }
        if (!optionalAccountTo.isPresent()) {
            // L'utilisateur utilise un id de compte destinataire n'existant pas
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
        Double amountInRecieverMoney = transfertinput.getAmount()* Exchangerate.exchangeRate.get(optionalAccountTo.get().getCountry());
        Double moneyBeforeCredit = Double.parseDouble(creditedCard.getCash());
        Double moneyAfterCredit = moneyBeforeCredit + amountInRecieverMoney;
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
