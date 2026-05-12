package com.credithandler.dossier.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperties {
    private String host;

    private int port;

    private String username;

    private String password;

    private Map<String, String> properties = new HashMap<>();
}
