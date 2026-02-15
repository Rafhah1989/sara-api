package com.sara.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sara API")
                        .version("1.0")
                        .description(
                                "Documentação das APIs do sistema SARA (Sistema de Automação de Registros Administrativos).")
                        .contact(new Contact()
                                .name("Rafhah1989")
                                .url("https://github.com/Rafhah1989")));
    }
}
