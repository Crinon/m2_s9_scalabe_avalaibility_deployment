package org.miage.bankservice.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.miage.bankservice.boundary.AccountRepresentation;
import org.miage.bankservice.boundary.TransfertRepresentation;
import org.miage.bankservice.entity.Transfert;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TransfertAssembler implements RepresentationModelAssembler<Transfert, EntityModel<Transfert>> {

    @Override
    public EntityModel<Transfert> toModel(Transfert transfert) {
        return EntityModel.of(transfert,
                linkTo(methodOn(TransfertRepresentation.class)
                        .getOneTransfert(String.valueOf(transfert.getIdtransfert()))).withSelfRel(),
                linkTo(methodOn(AccountRepresentation.class)
                        .getOneAccount(transfert.getAccountFrom().getId())).withRel("account_sender"),
                linkTo(methodOn(AccountRepresentation.class)
                        .getOneAccount(transfert.getAccountTo().getId())).withRel("account_receiver"),
                linkTo(methodOn(TransfertRepresentation.class)
                        .getAllTransferts()).withRel("collection"));
    }

    public CollectionModel<EntityModel<Transfert>> toCollectionModel(Iterable<? extends Transfert> entities) {
        List<EntityModel<Transfert>> transfertModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i -> toModel(i))
                .collect(Collectors.toList());
        return CollectionModel.of(transfertModel,
                linkTo(methodOn(TransfertRepresentation.class)
                        .getAllTransferts()).withSelfRel());
    }
}
