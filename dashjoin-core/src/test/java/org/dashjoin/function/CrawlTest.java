package org.dashjoin.function;

import java.io.FileNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CrawlTest {

  @Test
  public void testcrawl() throws Exception {
    Crawl r = new Crawl();
    Assertions.assertThrows(FileNotFoundException.class, () -> {
      r.run("file:upload");
    });
  }
}
