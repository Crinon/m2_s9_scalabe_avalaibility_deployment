package org.miage.bankservice.boundary;

import lombok.RequiredArgsConstructor;
import org.miage.bankservice.assembler.TransfertAssembler;
import org.miage.bankservice.entity.*;
import org.miage.bankservice.miscellaneous.CustomErrorHandler;
import org.miage.bankservice.miscellaneous.Exchangerate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping(value = "/transferts", produces = MediaType.APPLICATION_JSON_VALUE)
@RolesAllowed("ROLE_USER")
@RequiredArgsConstructor
@ExposesResourceFor(Transfert.class)
public class TransfertRepresentation extends CustomErrorHandler {
    private final TransfertResource transfertResource;
    private final CardResource cardResource;
    private final AccountResource accountResource;
    private final TransfertAssembler transfertAssembler;

    private static final Logger LOGGER = LoggerFactory.getLogger(TransfertRepresentation.class);

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllTransferts() {
        // ID du compte connecté
        String connectedAccountId = accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get().getId();
        // Ensemble des opérations dans lesquelles figure le compte connecté
        List<Transfert> transferts = transfertResource.findByAccountFrom_IdEqualsIgnoreCaseOrAccountTo_IdEqualsIgnoreCase(connectedAccountId,connectedAccountId);
        return ResponseEntity.ok(transfertAssembler.toCollectionModel(transferts));    }

    // GET one
    @GetMapping(value = "/{transfertId}")
    public ResponseEntity<?> getOneTransfert(@PathVariable("transfertId") String transfertId) {
        // ID du compte connecté
        String connectedAccountId = accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get().getId();
        Optional <Transfert> transfert = transfertResource.findById(transfertId);
        if(transfert.isPresent()){
            if(transfert.get().getAccountFrom().getId() == connectedAccountId ||transfert.get().getAccountTo().getId() == connectedAccountId ){
                return ResponseEntity.ok(transfertAssembler.toModel(transfert.get()));
            }
            String errorMessage = "{\"message\":\"Request not processed, reason is : transfert searched is not related to account connected\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }
        return ResponseEntity.notFound().build();
    }

    // GET all transfert from account
    @GetMapping(value = "/account/{accountId}")
    public ResponseEntity<?> getAllTransfertByAccount(@PathVariable("accountId") String requestedAccountId) {
        // L'opération doit concerner le compte connecté
        String connectedAccountId = accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get().getId();
        if(!requestedAccountId.equals(connectedAccountId)){
            String errorMessage = "{\"message\":\"Request not processed, reason is : cannot retrieve operations from another account\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }
        return ResponseEntity.ok(transfertAssembler.toCollectionModel(transfertResource.findByAccountFrom_IdEqualsIgnoreCaseOrAccountTo_IdEqualsIgnoreCase(requestedAccountId,requestedAccountId)));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> saveTransfert(@RequestBody @Valid TransfertInput transfertinput) {
        // Le transfert doit concerner le compte connecté
        String connectedAccountId = accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get().getId();
        if(!(connectedAccountId.equals(transfertinput.getIdaccountFrom()) || connectedAccountId.equals(transfertinput.getIdaccountTo()))){
            String errorMessage = "{\"message\":\"Request not processed, reason is : transfert must be related to accountid connected\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

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
            String errorMessage = "{\"message\":\"Request not processed, reason is : Receiver account not found\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // Si tout est OK alors on débite le sender avec la somme qu'il envoie
        Card senderCard = cardResource.findById(optionalAccountFrom.get().getFkidcard()).get();
        Double moneyBeforeDebit = Double.parseDouble(senderCard.getCash());
        Double moneyAfterDebit = moneyBeforeDebit - transfertinput.getAmount();
        senderCard.setCash(String.valueOf(moneyAfterDebit));
        cardResource.save(senderCard);

        // Si tout est OK alors on crédite le receiver avec la somme reçue convertie en monnaie locale
        Card receiverCard = cardResource.findById(optionalAccountTo.get().getFkidcard()).get();
        Double moneyBeforeCredit = Double.parseDouble(receiverCard.getCash());
        Double amountInReceiverMoney = transfertinput.getAmount()*Exchangerate.exchangeRate.get(optionalAccountTo.get().getCountry());
        Double moneyAfterCredit = moneyBeforeCredit + amountInReceiverMoney;
        receiverCard.setCash(String.valueOf(moneyAfterCredit));
        cardResource.save(receiverCard);

//      Si tout est OK alors on procède au transfert
        Transfert tranfert2Save = new Transfert(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                transfertinput.getAmount(),
                Exchangerate.exchangeRate.get(optionalAccountTo.get().getCountry()),
                optionalAccountFrom.get(),
                optionalAccountTo.get()
        );
        Transfert savedTransfert = transfertResource.save(tranfert2Save);

        URI location = linkTo(TransfertRepresentation.class).slash(savedTransfert.getIdtransfert()).toUri();
        return ResponseEntity.created(location).build();
    }

}
