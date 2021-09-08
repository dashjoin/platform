package org.dashjoin.function;

/**
 * expose sc.getUserPrincipal().getName() as a function
 */
public class DjUser extends AbstractEveryoneFunction<Void, String> {

  @Override
  public String run(Void arg) throws Exception {
    return sc.getUserPrincipal().getName();
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
