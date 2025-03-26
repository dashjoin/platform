package org.dashjoin.function;

import org.dashjoin.service.ACLContainerRequestFilter;

/**
 * expose sc.getUserPrincipal().getName() as a function
 */
public class DjUser extends AbstractEveryoneFunction<Void, String> {

  @Override
  public String run(Void arg) throws Exception {
    return ACLContainerRequestFilter.getEmail(sc);
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
