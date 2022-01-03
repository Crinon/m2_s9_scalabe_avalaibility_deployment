package org.miage.bankservice.boundary;

import org.miage.bankservice.assembler.CardAssembler;
import org.miage.bankservice.entity.Card;
import org.miage.bankservice.entity.CardInput;
import org.miage.bankservice.entity.CardValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value="/cards", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Card.class)
public class CardRepresentation {
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


    // PATCH
    @PatchMapping(value = "/{cardId}")
    @Transactional
    public ResponseEntity<?> updateCardPartiel(@PathVariable("cardId") String cardId,
                                                      @RequestBody Map<Object, Object> fields) {
        Optional<Card> body = cardResource.findById(cardId);
        if (body.isPresent()) {
            Card card = body.get();
            final boolean[] tryCash = {false};
            fields.forEach((f, v) -> {
                LOGGER.warn(f.toString());
                LOGGER.warn(v.toString());

                if (f.toString() == "cash") {
                    // L'utilisateur a tenté de PATCH son solde bancaire
                    tryCash[0] = true;
                }

                Field field = ReflectionUtils.findField(Card.class, f.toString());
                field.setAccessible(true);
                ReflectionUtils.setField(field, card, v);
            });
            if(tryCash[0] == true){
                // L'utilisateur a tenté de PATCH son solde bancaire
                String errorMessage = "{\"message\":\"Request not processed, reason is : You cannot create money\"}";
                return ResponseEntity.badRequest().body(errorMessage);
            }
            validator.validate(new CardInput(card.getNumber(), card.getCode(),
                    card.getCryptogram(), card.isBlocked(), card.isGps(), card.getSlidinglimit(), card.isContactless(), card.getCash()));
            card.setIdcard(cardId);
            cardResource.save(card);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

}
