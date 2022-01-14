package org.miage.bankservice.miscellaneous;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class ToolBox {
    // Fonction pour générer des suites de chiffres sous forme de String
    public static String generateCardNumber(int length) {
        String res = "";
        for (int i = 0 ; i<length; i++ ){
            res = res + (int) ((Math.random() * (9 - 0)));
        }
        return res;
    }

    public static String generateIBAN() {
        Random rand = new Random();
        // Création d'un string de 2 lettres
        String card = RandomStringUtils.random(2, true, false).toUpperCase();
        // Ajout des nombres
        for (int i = 0; i < 14; i++)
        {
            int n = rand.nextInt(10) + 0;
            card += Integer.toString(n);
        }
        return card;
    }

    // Génération d'un numéro de passport à partir d'une regex
//    public static String generatePassport() {
//        Random rand = new Random();
//        String res = "";
//        // Ajout des nombres
//        for (int i = 0; i < 2; i++)
//        {
//            int n = rand.nextInt(10) + 0;
//            res += Integer.toString(n);
//        }
//        // Ajout de 2 lettres
//        res += RandomStringUtils.random(2, true, false).toUpperCase();
//        // Ajout des nombres
//        for (int i = 0; i < 5; i++)
//        {
//            int n = rand.nextInt(10) + 0;
//            res += Integer.toString(n);
//        }
//        return res;
//    }

    public static String toJsonString(Object o) throws Exception {
        ObjectMapper map = new ObjectMapper();
        return map.writeValueAsString(o);
    }
}
