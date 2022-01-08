package org.miage.bankservice;

//      Une opération se fait entre deux comptes, on vérifie que les comptes spécifiés existent bien
// Une transaction ne peut se faire si le customer a bloqué sa carte
// On ne réalise pas la transaction si :
//      le customer utilise la sécurité GPS
//      le client et le shop ne sont pas dans le même pays
// On arrête si le customer dépasse la slidinglimit avec cette transaction

public class OperationTests {

    // Shop : payer avec le numéro de la carte directement avec un taux de 1.0

    // Payer avec un taux différent de 1.0

    // Refus de paiement si gpslocked et différents pays

    // Refus de paiement si sliding limit dépassé

    // id from faux

    // id to faux
}
