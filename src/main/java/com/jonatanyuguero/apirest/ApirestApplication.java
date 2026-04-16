package com.jonatanyuguero.apirest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info =
@Info(description = "Una API REST sobre tareas",
		version = "1.0",
		contact = @Contact(email = "jonatanstrunilina@gmail.com", name = "Jonatan"),
		license = @License(name = "CC BY"),
		title = "API sobre tareas"
)
)










@SpringBootApplication
public class ApirestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApirestApplication.class, args);
	}

}
