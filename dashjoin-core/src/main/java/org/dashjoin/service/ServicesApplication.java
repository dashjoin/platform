package org.dashjoin.service;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import lombok.extern.java.Log;

@ApplicationScoped
@ApplicationPath("/")
@Log
public class ServicesApplication extends Application {

}
