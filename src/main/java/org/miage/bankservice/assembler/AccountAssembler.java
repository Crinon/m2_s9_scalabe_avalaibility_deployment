package org.miage.bankservice.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.miage.bankservice.boundary.CardRepresentation;
import org.miage.bankservice.entity.Account;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.miage.bankservice.boundary.AccountRepresentation;

@Component
public class AccountAssembler implements RepresentationModelAssembler<Account, EntityModel<Account>> {
//    _links de l'account
//    _links de la carte de l'account
  @Override
  public EntityModel<Account> toModel(Account account) {
    return EntityModel.of(account,
            linkTo(methodOn(AccountRepresentation.class)
                        .getOneAccount(account.getId())).withSelfRel(),
            linkTo(methodOn(CardRepresentation.class)
                        .getOneCard(account.getFkidcard())).withSelfRel()
                        .withRel("card"),
			linkTo(methodOn(AccountRepresentation.class)
			            .getAllAccounts()).withRel("collection"));
  }

    //    _links de tous les accounts
  public CollectionModel<EntityModel<Account>> toCollectionModel(Iterable<? extends Account> entities) {
      List<EntityModel<Account>> accountModel = StreamSupport
        				.stream(entities.spliterator(), false)
        				.map(i -> toModel(i))
        				.collect(Collectors.toList());
      return CollectionModel.of(accountModel,                                					
              linkTo(methodOn(AccountRepresentation.class)
               			.getAllAccounts()).withSelfRel());
  }
}