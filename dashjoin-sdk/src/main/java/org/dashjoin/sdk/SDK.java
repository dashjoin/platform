package org.dashjoin.sdk;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;

/**
 * JAX-RS application with default security settings
 */
// @SecuritySchemes(value = {@SecurityScheme(securitySchemeName = "basic", type =
// SecuritySchemeType.HTTP, scheme = "basic")})
// @SecurityRequirement(name = "basic")
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
public class SDK extends Application {

}
