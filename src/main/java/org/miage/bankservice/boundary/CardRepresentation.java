package org.miage.bankservice.boundary;

import org.miage.bankservice.assembler.CardAssembler;
import org.miage.bankservice.entity.Card;import org.miage.bankservice.miscellaneous.CustomErrorHandler;

import org.miage.bankservice.entity.CardInput;
import org.miage.bankservice.entity.CardValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Field;
import java.util.*;

@RestController
@RequestMapping(value="/cards", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Card.class)
public class CardRepresentation extends CustomErrorHandler {
    private final CardResource cardResource;
    private final CardAssembler assembler;
    private final CardValidator validator;
    private static final Logger LOGGER= LoggerFactory.getLogger(CardRepresentation.class);

    public CardRepresentation(CardResource resource,
                                 CardAssembler assembler,
                                 CardValidator validator) {
        this.cardResource = resource;
        this.assembler = assembler;
        this.validator = validator;
    }

    // GET all
    @GetMapping
    public ResponseEntity<?> getAllCards() {
        return ResponseEntity.ok(assembler.toCollectionModel(cardResource.findAll()));
    }

    // GET one
    @GetMapping(value="/{cardId}")
    public ResponseEntity<?> getOneCard(@PathVariable("cardId") String id) {
        return Optional.ofNullable(cardResource.findById(id)).filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(assembler.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET one
    @GetMapping(value="/cardid/{cardNumber}")
    public ResponseEntity<?> getOneCardidByNumber(@PathVariable("cardNumber") String cardnumber) {
        return Optional.ofNullable(cardResource.findByNumberEqualsIgnoreCase(cardnumber)).filter(Optional::isPresent)
                .map(i -> ResponseEntity.ok(assembler.toModel(i.get())))
                .orElse(ResponseEntity.notFound().build());
    }


    // PATCH
    @PatchMapping(value = "/{cardId}")
    @Transactional
    public ResponseEntity<?> patchCard(@PathVariable("cardId") String cardId,
                                                      @RequestBody Map<Object, Object> fields) {
        Optional<Card> body = cardResource.findById(cardId);
        if (body.isPresent()) {
            Card card = body.get();
            final boolean[] tryCash = {false};
            final boolean[] needBool = {false};
            final boolean[] tryProtectedField = {false};
            final boolean[] needInteger = {false};
            final boolean[] notnull = {false};
            // On procède à la vérification des champs
            fields.forEach((f, v) -> {
                LOGGER.warn(f.toString());
                if (v == null) {
                    // L'utilisateur a tenté de PATCH son solde bancaire
                    notnull[0] = true;
                }
                if (v != null) LOGGER.warn(v.toString());
                if (f.toString() == "cash") {
                    // L'utilisateur a tenté de PATCH son solde bancaire
                    tryCash[0] = true;
                }
                if (Arrays.asList(new String[] {"cryptogram","code","number"}).contains(f.toString())) {
                    // L'utilisateur a tenté de PATCH un champ non autorisé
                    tryProtectedField[0] = true;
                }
                if ((Arrays.asList(new String[] {"contactless","regionLocked","blocked"}).contains(f.toString())) && !(v instanceof Boolean)) {
                    // L'utilisateur doit avoir utilisé un booléen mais ne l'a pas fait
                    needBool[0] = true;
                }
                // Si le champ est slidinglimit et que la valeur n'est pas un entier
                if (f.toString() == "slidinglimit" && !(v instanceof Integer)) {
                    needInteger[0] = true;
                }
            });
            if(tryCash[0] == true){
                // L'utilisateur a tenté de PATCH son solde bancaire
                String errorMessage = "{\"message\":\"Request not processed, reason is : You cannot create money\"}";
                return ResponseEntity.badRequest().body(errorMessage);
            }
            if(tryProtectedField[0] == true){
                // L'utilisateur a tenté de PATCH son solde bancaire
                String errorMessage = "{\"message\":\"Request not processed, reason is : You cannot change any other field that \"contactless\",\"gps\",\"blocked\",\"slidinglimit\"\"}";
                return ResponseEntity.badRequest().body(errorMessage);
            }
            if(needBool[0] == true){
                // L'utilisateur a tenté de PATCH son solde bancaire
                String errorMessage = "{\"message\":\"Request not processed, reason is : fields \"contactless\",\"gps\",\"blocked\" need boolean\"}";
                return ResponseEntity.badRequest().body(errorMessage);
            }
            if(needInteger[0] == true){
                // L'utilisateur a tenté de PATCH son solde bancaire
                String errorMessage = "{\"message\":\"Request not processed, reason is : slidinglimit requires integer\"}";
                return ResponseEntity.badRequest().body(errorMessage);
            }
            if(notnull[0] == true){
                // L'utilisateur a utilisé une valeur nulle
                String errorMessage = "{\"message\":\"Request not processed, reason is : null not allowed\"}";
                return ResponseEntity.badRequest().body(errorMessage);
            }
            fields.forEach((f, v) -> {
                        Field field = ReflectionUtils.findField(Card.class, f.toString());
                        field.setAccessible(true);
                        ReflectionUtils.setField(field, card, v);
                    });

                validator.validate(new CardInput(card.getNumber(), card.getCode(),
                    card.getCryptogram(), card.isBlocked(), card.isRegionLocked(), card.getSlidinglimit(), card.isContactless(), card.getCash()));
            card.setIdcard(cardId);
            cardResource.save(card);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }



}
