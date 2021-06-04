package org.dashjoin.function;

import static java.util.Arrays.asList;
import static org.dashjoin.util.MapUtil.of;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class FlattenTest {

  @Test
  public void test() throws Exception {
    Flatten f = new Flatten();
    List<Map<String, Object>> res = f.run(asList(of("a", 1, "l", asList(of("x", 1), of("x", 2))),
        of("a", 2, "l", asList(of("x", 3))), of("a", 3)));
    Assert.assertEquals("[{x=1, a=1}, {x=2, a=1}, {x=3, a=2}]", res.toString());
  }
}
