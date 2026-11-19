package org.dashjoin.function;

/**
 * shorthand for $read('config', 'dj-library', id).expression
 */
public class Use extends AbstractFunction<String, String> {

  @Override
  public String run(String id) throws Exception {
    var res = this.expressionService.getData().read(sc, "config", "dj-library", id);
    return (String) res.get("expression");
  }

  @Override
  public Class<String> getArgumentClass() {
    return String.class;
  }
}
