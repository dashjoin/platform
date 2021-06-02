package org.dashjoin.service.api;

import org.dashjoin.service.Services;
import org.eclipse.microprofile.openapi.annotations.Components;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * OpenAPI annotations to define API meta data and authorization options on the Swagger UI
 */
@OpenAPIDefinition(security = {@SecurityRequirement(name = "Dashjoin Auth")},
    tags = {@Tag(name = "default", description = "Dashjoin Platform API")},
    info = @Info(title = "Dashjoin API", version = "0.0.1",
        contact = @Contact(name = "Dashjoin", url = "https://github.com/dashjoin",
            email = "info@dashjoin.com")),
    components = @Components(securitySchemes = {@SecurityScheme(type = SecuritySchemeType.HTTP,
        securitySchemeName = "Dashjoin Auth", scheme = "basic")}))
public class ApiServices extends Services {
}
