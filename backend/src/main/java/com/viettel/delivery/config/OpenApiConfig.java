package com.viettel.delivery.config;

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

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI deliveryOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("He thong quan ly giao hang - API")
                        .version("1.0.0")
                        .description("""
                                API cua he thong quan ly giao hang (Viettel Software Internship).

                                Cach su dung:
                                1. Goi POST /auth/login de lay accessToken.
                                2. Bam nut Authorize o goc phai va dan token vao.
                                3. Moi response deu theo format {code, message, data}.
                                4. Them header Accept-Language: vi hoac en de doi ngon ngu thong bao loi.
                                """)
                        .contact(new Contact().name("Delivery Management Team"))
                        .license(new License().name("Internal use")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
