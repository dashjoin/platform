package org.dashjoin.function;

import java.security.Principal;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * expose sc.getUserPrincipal().getName() as a function
 */
public class DjUser extends AbstractEveryoneFunction<Void, String> {

  @Override
  public String run(Void arg) throws Exception {
    Principal up = sc.getUserPrincipal();
    if (up instanceof JsonWebToken)
      return ((JsonWebToken) up).getClaim("email");
    else
      return up.getName();
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "djUser";
  }
}
