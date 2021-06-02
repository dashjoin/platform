package org.dashjoin.function;

import java.util.Arrays;
import org.dashjoin.util.MapUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class AbstractMultiInputFunctionTest {

  @Test
  public void single() throws Exception {
    check("s", "s");
  }

  @Test
  public void list() throws Exception {
    check(Arrays.asList(1, 2, 3), "[1, 2, 3]");
  }

  @Test
  public void map() throws Exception {
    check(MapUtil.of("in", "s"), "{in=s, out=s}");
  }

  @Test
  public void mapList() throws Exception {
    check(MapUtil.of("in", Arrays.asList(1, 2)), "[{in=1, out=1}, {in=2, out=2}]");
  }

  @Test
  public void mapNull() throws Exception {
    check(MapUtil.of("in", null), "{in=null}");
  }

  @Test
  public void listMap() throws Exception {
    check(Arrays.asList(MapUtil.of("in", "s"), MapUtil.of("in", "s")),
        "[{in=s, out=s}, {in=s, out=s}]");
  }

  void check(Object o, String d) throws Exception {
    TestMultiInputFunction f = new TestMultiInputFunction();
    Assert.assertEquals(d, f.run(o).toString());
  }

  public static class TestMultiInputFunction extends AbstractMultiInputFunction {

    @Override
    public String getID() {
      return null;
    }

    @Override
    public String getType() {
      return null;
    }

    @Override
    public Object single(Object arg) throws Exception {
      return arg;
    }

    @Override
    public String inputField() {
      return "in";
    }

    @Override
    public String outputField() {
      return "out";
    }
  }
}
