package org.dashjoin.function;

import java.util.Arrays;
import org.dashjoin.util.MapUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class Doc2dataTest {

  @Test
  public void single() throws Exception {
    Doc2data f = new Doc2data();
    Assert.assertEquals("{x=1}", f.run("{\"x\":1}").toString());
  }

  @Test
  public void list() throws Exception {
    Doc2data f = new Doc2data();
    Assert.assertEquals("[{x=1}]", f.run(Arrays.asList("{\"x\":1}")).toString());
  }

  @Test
  public void map() throws Exception {
    Doc2data f = new Doc2data();
    Assert.assertEquals("{x=1, url={\"x\":1}}", f.run(MapUtil.of("url", "{\"x\":1}")).toString());
  }

  @Test
  public void map2() throws Exception {
    Doc2data f = new Doc2data();
    Assert.assertEquals("[{x=1, url=[{\"x\":1}]}]",
        f.run(MapUtil.of("url", "[{\"x\":1}]")).toString());
  }
}
