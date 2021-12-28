package org.miage.bankservice.miscellaneous;

public class ToolBox {
    // Fonction pour générer des suites de chiffres sous forme de String
    public static String generate(int length) {
        String res = "";
        for (int i = 0 ; i<length; i++ ){
            res = res + (int) ((Math.random() * (9 - 0)));
        }
        return res;
    }
}
