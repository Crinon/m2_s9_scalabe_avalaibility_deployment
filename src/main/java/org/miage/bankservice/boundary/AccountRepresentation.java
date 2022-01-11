package org.miage.bankservice.boundary;

import lombok.RequiredArgsConstructor;
import org.miage.bankservice.assembler.AccountAssembler;
import org.miage.bankservice.entity.*;
import org.miage.bankservice.miscellaneous.ToolBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.miage.bankservice.miscellaneous.CustomErrorHandler;

import javax.annotation.security.RolesAllowed;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Account.class)
@RequiredArgsConstructor
public class AccountRepresentation extends CustomErrorHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRepresentation.class);

    private final AccountResource accountResource;
    private final AccountAssembler assembler;
    private final AccountValidator accountValidator;
    private final CardValidator cardValidator;
    private final CardResource cardResource;
    private final PasswordEncoder passwordEncoder;


    // GET all
    @GetMapping
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<?> getConnectedAccount() {
//        System.out.println(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization"));
        System.out.println(currentUsername());
        // Vérification de l'exitence de l'account
        if (accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get() == null) {
            String errorMessage = "{\"message\":\"Request not processed, reason is : Passport does not exist\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }
        return ResponseEntity.ok(assembler.toModel(accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get()));
    }

    // GET one
    @RolesAllowed("ROLE_USER")
    @GetMapping(value = "/{accountId}")
    public ResponseEntity<?> getOneAccount(@PathVariable("accountId") String id) {
        return Optional.ofNullable(accountResource.findById(id)).filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(assembler.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> saveAccount(@RequestBody @Valid AccountInput account) {

        // On vérifie que le passeport donné n'est pas déjà utilisé
        Optional<Account> optionalAccountPassport = accountResource.findByPassportNumberEqualsIgnoreCase(account.getPassportNumber());
        if (optionalAccountPassport.isPresent()) {
            String errorMessage = "{\"message\":\"Request not processed, reason is : Passport already used\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

        // On vérifie que le numéro de téléphone donné n'est pas déjà utilisé
        Optional<Account> optionalAccountPhone = accountResource.findByPhoneGlobalEqualsIgnoreCase(account.getPhoneGlobal());
        if (optionalAccountPhone.isPresent()) {
            String errorMessage = "{\"message\":\"Request not processed, reason is : Phone number already used\"}";
            return ResponseEntity.badRequest().body(errorMessage);
        }

        Card newCard = new Card();
        cardValidator.validate(new CardInput(newCard.getNumber(), newCard.getCode(),
                newCard.getCryptogram(), newCard.isBlocked(), newCard.isRegionLocked(), newCard.getSlidinglimit(), newCard.isContactless(), newCard.getCash()));

        Account account2Save = new Account(
                UUID.randomUUID().toString(),
                account.getName(),
                account.getSurname(),
                account.getCountry(),
                account.getPassportNumber(),
                account.getPhoneGlobal(),
                ToolBox.generateIBAN(),
                newCard.getIdcard(),
                account.getTransfertsReceived(),
                account.getTransfertsSent(),
                passwordEncoder.encode(account.getPassword())
        );
        cardResource.save(newCard);
        Account saved = accountResource.save(account2Save);
        URI location = linkTo(AccountRepresentation.class).slash(saved.getId()).toUri();
        return ResponseEntity.created(location).build();
    }


    // PATCH
    @PatchMapping(value = "/{accountId}")
    @Transactional
    @RolesAllowed("ROLE_USER")
    public ResponseEntity<?> patchAccount(@PathVariable("accountId") String accountId,
                                          @RequestBody Map<Object, Object> fields) {
        Optional<Account> body = accountResource.findById(accountId);

        // Des données de patch doivent être présentent et le compte demandé en paramètre doit exister en base
        if (body.isPresent() && (accountResource.findById(accountId).get() != null)) {
            Account account = body.get();
            // On vérifie que le compte connecté est bien celui à patcher => accountId
            if (accountResource.findByPassportNumberEqualsIgnoreCase(currentUsername()).get().getId() != accountId) {
                String errorMessage = "{\"message\":\"Request not processed, reason is : accountid provided does not match with account id connected\"}";
                return ResponseEntity.badRequest().body(errorMessage);
            }

            final boolean[] tryProtectedField = {false};
            fields.forEach((f, v) -> {
                LOGGER.warn(f.toString());
                LOGGER.warn(v.toString());

                // Un utilisateur ne peut que modifier un de ces 3 champs
                if (!Arrays.asList(new String[]{"name", "surname", "phoneGlobal"}).contains(f.toString())) {
                    // L'utilisateur a tenté de PATCH un champ non autorisé
                    tryProtectedField[0] = true;
                }


                if (tryProtectedField[0] == false) {
                    Field field = ReflectionUtils.findField(Account.class, f.toString());
                    field.setAccessible(true);
                    ReflectionUtils.setField(field, account, v);
                }

            });
            if (tryProtectedField[0] == true) {
                // L'utilisateur a tenté de PATCH un champ non autorisé
                String errorMessage = "{\"message\":\"Request not processed, reason is : You cannot change any other field that \"name\",\"surname\",\"phoneGlobal\"\"}";
                return ResponseEntity.badRequest().body(errorMessage);
            }
            accountValidator.validate(new AccountInput(account.getName(), account.getSurname(), account.getCountry(),
                    account.getPassportNumber(),
                    account.getPhoneGlobal(),
                    account.getIban(),
                    cardResource.getById(account.getFkidcard()), account.getTransfertsReceived(), account.getTransfertsSent(), passwordEncoder.encode(account.getPassword())));
            account.setId(accountId);
            accountResource.save(account);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

}
