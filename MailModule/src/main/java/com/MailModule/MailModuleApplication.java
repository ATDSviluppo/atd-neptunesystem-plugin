package com.MailModule;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@AutoConfigurationPackage
@ComponentScan("com.MailModule")
@EnableScheduling
public class MailModuleApplication {
	@PostConstruct
	public void init() {
		System.out.println("=== MAIL MODULE CARICATO ===");
	}
}
