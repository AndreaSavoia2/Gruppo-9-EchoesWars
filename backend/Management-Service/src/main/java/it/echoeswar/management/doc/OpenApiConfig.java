package it.echoeswar.management.doc;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures shared OpenAPI metadata and the JWT bearer authentication scheme.
 *
 * <p>Security is declared on individual protected operations so that public
 * authentication and catalogue endpoints remain callable from Swagger UI.</p>
 */
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT access token issued by the authentication endpoints.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
@Configuration
public class OpenApiConfig {

    /**
     * Provides the metadata shared by every generated OpenAPI document.
     *
     * @return the configured OpenAPI document
     */
    @Bean
    public OpenAPI defineOpenApi() {

        Server serverDev = new Server();
        serverDev.setUrl("http://localhost:8080");
        serverDev.setDescription("Local development server");

        Info info = new Info()
                .title("Echoes'War Management System API")
                .version("0.1")
                .description("REST API documentation for the Echoes'War management system.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(serverDev));
    }
}
