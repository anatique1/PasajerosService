package com.example.pasajero.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pasajeroOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Pasajeros (RutaMacht)")
                        .version("1.0.0")
                        .description("Microservicio de pasajeros de RutaMacht: CRUD de pasajeros persistido en "
                                + "MongoDB e inicio de sesión con Google. APIRutaMacht (puerto 8081) lo "
                                + "consume mediante RestClient."))
                .servers(List.of(new Server().url("http://localhost:8082").description("Local")));
    }
}
