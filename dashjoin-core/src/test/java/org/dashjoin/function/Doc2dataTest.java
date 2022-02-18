package org.dashjoin.function;

import java.io.FileNotFoundException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import javax.inject.Inject;
import javax.ws.rs.core.SecurityContext;
import org.apache.commons.io.input.ReaderInputStream;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.util.MapUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class Doc2dataTest {

  @Inject
  ExpressionService expr;

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
    Assertions.assertEquals("{c={x=1, y=2}}", res.toString());
  }

  @Test
  public void xmlArr() throws Exception {
    Doc2data f = new Doc2data();
    Object res = f.parse("<?xml version=\"1.0\"?><c x=\"1\"><y>2</y><y>3</y></c>");
    Assertions.assertEquals("{c={x=1, y=[2, 3]}}", res.toString());
  }

  @Test
  public void xmlArr2() throws Exception {
    Doc2data f = new Doc2data();
    Object res = f.parse(
        "<?xml version=\"1.0\"?><c x=\"1\"><list><y>2</y></list><list><y>2</y><y>2</y><y>2</y></list></c>");
    Assertions.assertEquals("{c={x=1, list=[{y=[2]}, {y=[2, 2, 2]}]}}", res.toString());
  }

  @Test
  public void testCleanArrays() {
    Doc2data f = new Doc2data();
    Object res = f.cleanArrays(MapUtil.of("x", Arrays.asList(1, 2)));
    Assertions.assertEquals("{x=[1, 2]}", res.toString());
  }

  @Test
  public void testCleanArrays2() {
    Doc2data f = new Doc2data();
    Object res = f.cleanArrays(
        MapUtil.of("x", Arrays.asList(MapUtil.of("y", 1), MapUtil.of("y", Arrays.asList(1, 2)))));
    Assertions.assertEquals("{x=[{y=[1]}, {y=[1, 2]}]}", res.toString());
  }

  @Test
  public void csv() throws Exception {
    Doc2data f = new Doc2data();
    Object res = f.parse("id,name\n123,joe");
    Assertions.assertEquals("[{name=joe, id=123}]", res.toString());
  }

  @Test
  public void single() throws Exception {
    Doc2data f = new Doc2data();
    Assertions.assertEquals("{x=1}", f.run("{\"x\":1}").toString());
  }

  @Test
  public void list() throws Exception {
    Doc2data f = new Doc2data();
    Assertions.assertEquals("[{x=1}]", f.run("[{\"x\":1}]").toString());
  }

  @Test
  public void map() throws Exception {
    Doc2data f = new Doc2data();
    Assertions.assertEquals("{x=1}", f.run("{\"x\":1}").toString());
  }

  @Test
  public void json1() throws Exception {

    Doc2data f = new Doc2data();

    String s = "{\"a\":1,\"b\":2}{\"a\":4,\"b\":8}{\"a\":5,\"b\":9}";
    Object res =
        f.parseJson(new ReaderInputStream(new StringReader(s), Charset.defaultCharset()), true);
    Assertions.assertEquals("[{a=1, b=2}, {a=4, b=8}, {a=5, b=9}]", "" + res);

    s = "[1,2,3][4,5,6][7,8,9]";
    res = f.parseJson(new ReaderInputStream(new StringReader(s), Charset.defaultCharset()), true);
    Assertions.assertEquals("[1, 2, 3, 4, 5, 6, 7, 8, 9]", "" + res);

    s = "[{\"a\":1,\"b\":2,\"c\":3}][{\"a\":4,\"b\":5,\"c\":6}][{\"a\":7,\"b\":8,\"c\":9}]";
    res = f.parseJson(new ReaderInputStream(new StringReader(s), Charset.defaultCharset()), true);;
    Assertions.assertEquals("[{a=1, b=2, c=3}, {a=4, b=5, c=6}, {a=7, b=8, c=9}]", "" + res);
  }

  @Test
  public void json2() throws Exception {

    Doc2data f = new Doc2data();

    // array flattening = false

    String s = "{\"a\":1,\"b\":2}{\"a\":4,\"b\":8}{\"a\":5,\"b\":9}";
    Object res =
        f.parseJson(new ReaderInputStream(new StringReader(s), Charset.defaultCharset()), false);
    Assertions.assertEquals("[{a=1, b=2}, {a=4, b=8}, {a=5, b=9}]", "" + res);

    s = "[1,2,3][4,5,6][7,8,9]";
    res = f.parseJson(new ReaderInputStream(new StringReader(s), Charset.defaultCharset()), false);
    Assertions.assertEquals("[[1, 2, 3], [4, 5, 6], [7, 8, 9]]", "" + res);

    s = "[{\"a\":1,\"b\":2,\"c\":3}][{\"a\":4,\"b\":5,\"c\":6}][{\"a\":7,\"b\":8,\"c\":9}]";
    res = f.parseJson(new ReaderInputStream(new StringReader(s), Charset.defaultCharset()), false);;
    Assertions.assertEquals("[[{a=1, b=2, c=3}], [{a=4, b=5, c=6}], [{a=7, b=8, c=9}]]", "" + res);
  }

  @Test
  public void jsonata() throws Exception {
    SecurityContext sc = Mockito.mock(SecurityContext.class);
    Mockito.when(sc.isUserInRole(ArgumentMatchers.anyString())).thenReturn(true);
    Object res = expr.resolve(sc, "[\"1\", \"2\"].$doc2data($&\"1\").{\"output\": $}", null);
    Assertions.assertEquals("[{output=11}, {output=21}]", res.toString());
  }
}
