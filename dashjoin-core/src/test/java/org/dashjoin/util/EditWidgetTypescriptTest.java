package org.dashjoin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class EditWidgetTypescriptTest {

  @Test
  public void testWidgets() throws IOException {
    Set<String> edit = new HashSet<>();
    for (String line : getLines(
        "../angular/src/app/edit-widget-dialog/edit-widget-dialog.component.ts")) {
      line = line.trim();
      // 'upload',
      if (line.startsWith("'") && line.endsWith("',"))
        edit.add(line.substring(1, line.length() - 2));
    }

    Set<String> widget = new HashSet<>();
    for (String line : getLines("../angular/src/app/instance/widget.ts")) {
      line = line.trim();
      // 'upload'
      if (line.startsWith("'") && line.endsWith("'"))
        widget.add(line.substring(1, line.length() - 1));
    }

    System.out.println("editable but not defined");
    System.out.println(minus(edit, widget));

    System.out.println("defined but not editable");
    System.out.println(minus(widget, edit));
  }

  @Test
  public void testFields() throws IOException {
    Set<String> edit = new HashSet<>();
    for (String line : getLines(
        "../angular/src/app/edit-widget-dialog/edit-widget-dialog.component.ts")) {
      line = line.trim();
      // database: {
      if (line.matches("[a-zA-Z]+: \\{"))
        edit.add(line.split(":")[0]);
    }

    // remove wrong match
    edit.remove("additionalProperties");

    Set<String> widget = new HashSet<>();
    widget.add("widget");
    for (String line : getLines("../angular/src/app/instance/widget.ts")) {
      line = line.trim();
      // database: string;
      if (line.matches("[a-zA-Z]+:.*;"))
        widget.add(line.split(":")[0]);
    }

    // these are legal but undefined since these are reserved JS keywords
    widget.add("if");
    widget.add("foreach");

    // defined but not editable since these are special cases
    widget.remove("readOnly");
    widget.remove("children");

    System.out.println("editable but not defined");
    System.out.println(minus(edit, widget));
    Assert.assertTrue(minus(edit, widget).isEmpty());

    System.out.println("defined but not editable");
    System.out.println(minus(widget, edit));
  }

  String[] getLines(String file) throws IOException {
    String edit = IOUtils.toString(new FileInputStream(new File(file)), Charset.defaultCharset());
    return edit.split("\n");
  }

  Set<String> minus(Set<String> a, Set<String> b) {
    Set<String> tmp = new HashSet<>(a);
    tmp.removeAll(b);
    return tmp;
  }
}
