package org.miage.bankservice.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.miage.bankservice.boundary.CardRepresentation;
import org.miage.bankservice.entity.Card;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class CardAssembler implements RepresentationModelAssembler<Card, EntityModel<Card>> {

    // Méthodes pour construire l'url href SELF puis COLLECTION

    @Override
    public EntityModel<Card> toModel(Card card) {
        return EntityModel.of(card,
                linkTo(methodOn(CardRepresentation.class)
                        .getOneCard(String.valueOf(card.getIdcard()))).withSelfRel(),
                linkTo(methodOn(CardRepresentation.class)
                        .getAccountConnectedCard()).withRel("collection"));
    }

    public CollectionModel<EntityModel<Card>> toCollectionModel(Iterable<? extends Card> entities) {
        List<EntityModel<Card>> cardModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i -> toModel(i))
                .collect(Collectors.toList());
        return CollectionModel.of(cardModel,
                linkTo(methodOn(CardRepresentation.class)
                        .getAccountConnectedCard()).withSelfRel());
    }
}