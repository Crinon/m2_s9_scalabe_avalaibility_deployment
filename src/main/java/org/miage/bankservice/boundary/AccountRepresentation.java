package org.miage.bankservice.boundary;

import org.miage.bankservice.assembler.AccountAssembler;
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
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value="/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Account.class)
public class AccountRepresentation {
    private static final Logger LOGGER=LoggerFactory.getLogger(AccountRepresentation.class);

    private final AccountResource accountResource;
    private final AccountAssembler assembler;
    private final CardValidator cardValidator;
    private final CardResource cardResource;

    public AccountRepresentation(AccountResource accountResource,
                                 CardResource cardResource,
                                 CardValidator cardValidator,
                                 AccountAssembler assembler) {
        this.accountResource = accountResource;
        this.assembler = assembler;
        this.cardValidator = cardValidator;
        this.cardResource = cardResource;
    }

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllAccounts() {
        return ResponseEntity.ok(assembler.toCollectionModel(accountResource.findAll()));
    }

    // GET one
    @GetMapping(value="/{accountId}")
    public ResponseEntity<?> getOneAccount(@PathVariable("accountId") String id) {
        return Optional.ofNullable(accountResource.findById(id)).filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(assembler.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> saveAccount(@RequestBody @Valid AccountInput account)  {
        Card newCard = new Card();
        cardValidator.validate(new CardInput(newCard.getNumber(), newCard.getCode(),
                newCard.getCryptogram(), newCard.isBlocked(), newCard.isGps(), newCard.getSlidinglimit(), newCard.isContactless(), newCard.getCash()));
        LOGGER.warn(newCard.getNumber());

        Account account2Save = new Account(
            UUID.randomUUID().toString(),
                account.getName(),
                account.getSurname(),
                account.getCountry(),
                account.getPassportNumber(),
                account.getPhoneGlobal(),
                account.getIban(),
                newCard.getIdcard(),
                account.getTransfertsReceived(),
                account.getTransfertsSent()
        );
        Card cardsaved =cardResource.save(newCard);
        Account saved = accountResource.save(account2Save);
        URI location = linkTo(AccountRepresentation.class).slash(saved.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    // DELETE
    @DeleteMapping(value = "/{accountId}")
    @Transactional
    public ResponseEntity<?> deleteAccount(@PathVariable("accountId") String accountId) {
        Optional<Account> account = accountResource.findById(accountId);
        if (account.isPresent()) {
            accountResource.delete(account.get());
        }
        return ResponseEntity.noContent().build();
    }

    // PUT
    @PutMapping(value = "/{accountId}")
    @Transactional
    public ResponseEntity<?> updateAccount(@RequestBody Account account,
            @PathVariable("accountId") String accountId) {
        Optional<Account> body = Optional.ofNullable(account);
        if (!body.isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        if (!accountResource.existsById(accountId)) {
            return ResponseEntity.notFound().build();
        }
        account.setId(accountId);
        Account result = accountResource.save(account);
        return ResponseEntity.ok().build();
    }

}
