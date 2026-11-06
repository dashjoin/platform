package org.dashjoin.function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ERDiagramTest {

  @Test
  public void testNote() {
    ERDiagram e = new ERDiagram();
    Assertions.assertEquals("'title'", e.note("title", null));
    Assertions.assertEquals("'comment'", e.note(null, "comment"));
    Assertions.assertEquals("'title comment'", e.note("title", "comment"));
    Assertions.assertEquals("'it\\'s'", e.note(null, "it's"));
  }
}
