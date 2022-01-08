package org.miage.bankservice.miscellaneous;

import org.miage.bankservice.entity.Account;

import java.util.HashMap;
import java.util.Map;

public class Exchangerate {

    public static Map<Account.Country, Double> exchangeRate;
    static {
        exchangeRate = new HashMap<>();
        exchangeRate.put(Account.Country.FRANCE, 1.00);
        exchangeRate.put(Account.Country.GERMANY, 1.00);
        exchangeRate.put(Account.Country.ITALY, 1.00);
        exchangeRate.put(Account.Country.MOLDOVA, 1.00);
        exchangeRate.put(Account.Country.JAPAN, 0.0076); // Yen
        exchangeRate.put(Account.Country.USA, 0.88); // USDollar
        exchangeRate.put(Account.Country.UK, 1.20); // Pound
    }

}