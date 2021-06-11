package org.dashjoin.function;

import java.io.FileNotFoundException;
import java.util.Arrays;
import org.dashjoin.util.MapUtil;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class Doc2dataTest {

  @Test
  public void url() throws Exception {
    Assertions.assertThrows(FileNotFoundException.class, () -> {
      new Doc2data().run("file:upload/notthere.txt");
    });
  }

  @Test
  public void xml() throws Exception {
    Doc2data f = new Doc2data();
    Object res = f.parse("<?xml version=\"1.0\"?><c x=\"1\"><y>2</y></c>");
    Assert.assertEquals("{c={x=1, y=2}}", res.toString());
  }

  @Test
  public void csv() throws Exception {
    Doc2data f = new Doc2data();
    Object res = f.parse("id,name\n123,joe");
    Assert.assertEquals("[{name=joe, id=123}]", res.toString());
  }

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
