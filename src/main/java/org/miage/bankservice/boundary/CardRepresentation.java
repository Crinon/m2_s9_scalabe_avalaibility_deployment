package org.miage.bankservice.boundary;

import org.miage.bankservice.assembler.CardAssembler;
import org.miage.bankservice.entity.*;
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
@RequestMapping(value="/cards", produces = MediaType.APPLICATION_JSON_VALUE)
@ExposesResourceFor(Card.class)
public class CardRepresentation {
    private final CardResource cardResource;
    private final CardAssembler assembler;
    private final CardValidator validator;
    private static final Logger LOGGER= LoggerFactory.getLogger(AccountRepresentation.class);

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

            fields.forEach((f, v) -> {
                LOGGER.warn(f.toString());
                LOGGER.warn(v.toString());

                Field field = ReflectionUtils.findField(Card.class, f.toString());
                field.setAccessible(true);
                ReflectionUtils.setField(field, card, v);
            });
            validator.validate(new CardInput(card.getNumber(), card.getCode(),
                    card.getCryptogram(), card.isBlocked(), card.isGps(), card.getSlidinglimit(), card.isContactless()));
            card.setIdcard(cardId);
            cardResource.save(card);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

}
