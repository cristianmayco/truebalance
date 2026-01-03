package com.truebalance.truebalance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TrueBalance API")
                        .version("1.0.1")
                        .description("API para gerenciamento de contas e finanças pessoais. " +
                                    "Suporta criação de contas independentes ou vinculadas a cartões de crédito " +
                                    "com distribuição automática de parcelas em faturas.")
                        .contact(new Contact()
                                .name("TrueBalance Team")
                                .email("contato@truebalance.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
