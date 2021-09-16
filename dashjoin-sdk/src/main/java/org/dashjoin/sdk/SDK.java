package org.dashjoin.sdk;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

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
