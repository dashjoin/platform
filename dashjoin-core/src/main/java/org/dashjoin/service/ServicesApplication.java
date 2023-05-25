package org.dashjoin.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * 
 * JAX-RS Application class.
 * 
 * ServicesApplication needs to be separated from Services, because since 2.x Quarkus does not
 * allow @Inject into Application any more.
 * 
 * See: https://github.com/quarkusio/quarkus/pull/22440
 * 
 */
@ApplicationScoped
@ApplicationPath("/")
public class ServicesApplication extends Application {
  // Intentionally empty. Functionality is in Services class
}
