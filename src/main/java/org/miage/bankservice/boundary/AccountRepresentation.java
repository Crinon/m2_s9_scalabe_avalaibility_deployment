package org.miage.bankservice.boundary;

import org.miage.bankservice.assembler.AccountAssembler;
import org.miage.bankservice.entity.*;
import org.miage.bankservice.miscellaneous.LoggingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;
import javax.validation.Valid;

@RestController
@RequestMapping(value="/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Account.class)
public class AccountRepresentation {
    Logger logger = LoggerFactory.getLogger(LoggingController.class);
    private static final Logger LOGGER=LoggerFactory.getLogger(AccountRepresentation.class);

    private final AccountResource ir;
    private final AccountAssembler assembler;
    private final AccountValidator validator;
    private final CardValidator cardValidator;
    private final CardResource cardResource;

    public AccountRepresentation(AccountResource ir,
                                 CardResource cardResource,
                                 CardValidator cardValidator,
                                 AccountAssembler assembler,
                                 AccountValidator validator) {
        this.ir = ir;
        this.assembler = assembler;
        this.validator = validator;
        this.cardValidator = cardValidator;
        this.cardResource = cardResource;
    }

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllAccounts() {
        return ResponseEntity.ok(assembler.toCollectionModel(ir.findAll()));
    }

    // GET one
    @GetMapping(value="/{accountId}")
    public ResponseEntity<?> getOneAccount(@PathVariable("accountId") String id) {
        return Optional.ofNullable(ir.findById(id)).filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(assembler.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> saveAccount(@RequestBody @Valid AccountInput account)  {
        Card newCard = new Card();
        cardValidator.validate(new CardInput(newCard.getNumber(), newCard.getCode(),
                newCard.getCryptogram(), newCard.isBlocked(), newCard.isGps(), newCard.getSlidinglimit(), newCard.isContactless()));
        logger.warn(newCard.getNumber());

        Account account2Save = new Account(
            UUID.randomUUID().toString(),
                account.getName(),
                account.getSurname(),
                account.getCountry(),
                account.getPassportNumber(),
                account.getPhoneGlobal(),
                account.getIban(),
                newCard.getIdcard()
        );
        Card cardsaved =cardResource.save(newCard);
        Account saved = ir.save(account2Save);
        URI location = linkTo(AccountRepresentation.class).slash(saved.getId()).toUri();
        return ResponseEntity.created(location).build();
    }

    // DELETE
    @DeleteMapping(value = "/{accountId}")
    @Transactional
    public ResponseEntity<?> deleteAccount(@PathVariable("accountId") String accountId) {
        Optional<Account> account = ir.findById(accountId);
        if (account.isPresent()) {
            ir.delete(account.get());
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
        if (!ir.existsById(accountId)) {
            return ResponseEntity.notFound().build();
        }
        account.setId(accountId);
        Account result = ir.save(account);
        return ResponseEntity.ok().build();
    }

}
