package org.miage.bankservice.assembler;

import org.miage.bankservice.boundary.AccountRepresentation;
import org.miage.bankservice.boundary.OperationRepresentation;
import org.miage.bankservice.entity.Operation;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OperationAssembler {

    public EntityModel<Operation> toModel(Operation operation) {
        return EntityModel.of(operation,
                linkTo(methodOn(OperationRepresentation.class)
                        .getOneOperation(String.valueOf(operation.getId()))).withSelfRel(),
                linkTo(methodOn(AccountRepresentation.class)
                        .getOneAccount(operation.getAccountFrom().getId())).withRel("account_customer"),
                linkTo(methodOn(AccountRepresentation.class)
                        .getOneAccount(operation.getAccountTo().getId())).withRel("account_shop"),
                linkTo(methodOn(OperationRepresentation.class)
                        .getAllOperation()).withRel("collection"));
    }

    public CollectionModel<EntityModel<Operation>> toCollectionModel(Iterable<? extends Operation> entities) {
        List<EntityModel<Operation>> operationModel = StreamSupport
                .stream(entities.spliterator(), false)
                .map(i -> toModel(i))
                .collect(Collectors.toList());
        return CollectionModel.of(operationModel,
                linkTo(methodOn(OperationRepresentation.class)
                        .getAllOperation()).withSelfRel());
    }
}
