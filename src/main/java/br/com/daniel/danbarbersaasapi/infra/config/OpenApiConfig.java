package br.com.daniel.danbarbersaasapi.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Barber SaaS API")
                        .version("1.0.0")
                        .description("""
                                API REST para microSaaS de gestão de barbearias independentes.
                                
                                **Funcionalidades:**
                                - 📅 Agenda com calendário e validação de conflitos
                                - 👥 Gestão de clientes com controle de retorno
                                - 💬 Integração com WhatsApp
                                - 💰 Ordens de serviço e financeiro
                                - 📊 Dashboard e relatórios
                                - 🔒 Multi-tenancy — cada barbeiro vê apenas seus dados
                                """)
                        .contact(new Contact()
                                .name("Daniel")
                                .url("https://github.com/daniellhrt"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer JWT"))
                .components(new Components()
                        .addSecuritySchemes("Bearer JWT",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtido via POST /auth/login")));
    }
}
