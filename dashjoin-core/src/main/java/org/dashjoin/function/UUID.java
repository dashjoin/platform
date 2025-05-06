package org.dashjoin.function;

public class UUID extends AbstractFunction<Void, String> {

  @Override
  public String run(Void arg) throws Exception {
    return java.util.UUID.randomUUID().toString();
  }

  @Override
  public Class<Void> getArgumentClass() {
    return Void.class;
  }

  @Override
  public String getID() {
    return "uuid";
  }

  @Override
  public String getType() {
    return "read";
  }
}
