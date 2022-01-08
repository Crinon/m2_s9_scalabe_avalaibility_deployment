package org.miage.bankservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class ConfigProperties {

    @Autowired
    private static Environment env;

    public static String getConfigValue(String configKey){
        System.out.println(configKey);
        return env.getProperty(configKey);
    }
}
