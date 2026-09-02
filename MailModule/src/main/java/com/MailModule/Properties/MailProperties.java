package com.MailModule.Properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@ConfigurationProperties(prefix = "mail")
@PropertySource("file:./config/custom.properties")
public class MailProperties {

    @Value("${mail.daysBeforeExpiration}")
    private int daysBeforeExpiration;

    @Value("${mail.subject}")
    private String subject;

    @Value("${mail.body}")
    private String body;

    @Value("${mail.url}")
    private String url;

}
