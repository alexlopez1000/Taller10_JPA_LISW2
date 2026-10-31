package com.example.questionbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boots the Saber Pro Question Bank application.
 *
 * <p>The project follows a layered architecture: {@code api} exposes HTTP endpoints,
 * {@code application} coordinates use cases, {@code domain} holds technology-free
 * business objects and repository contracts, and {@code infrastructure} implements
 * those contracts with Spring Data JPA.</p>
 */
@SpringBootApplication
public class QuestionBankApplication {

    /** Starts the Spring Boot application. @param args command-line arguments */
    public static void main(String[] args) {
        SpringApplication.run(QuestionBankApplication.class, args);
    }
}
