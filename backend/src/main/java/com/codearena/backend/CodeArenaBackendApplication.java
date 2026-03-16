package com.codearena.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.codearena.backend.repository")
public class CodeArenaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodeArenaBackendApplication.class, args);
	}

}
