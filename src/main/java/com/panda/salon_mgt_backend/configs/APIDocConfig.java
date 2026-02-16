package com.panda.salon_mgt_backend.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Salon Management Application built by Abhisek Panda.",
                description = "A Multi-tenant Salon Management System",
                contact = @Contact(
                        name = "Abhisek Panda",
                        url = "https://www.linkedin.com/in/abhisek-panda-",
                        email = "abhisekpanda114@gmail.com"
                ),
                version = "1.0",
                summary = "This app is very useful for customers, and salon owner, workers."
        )
)
public class APIDocConfig {
}