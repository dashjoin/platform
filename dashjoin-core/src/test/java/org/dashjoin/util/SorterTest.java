package org.dashjoin.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.dashjoin.service.Manage.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SorterTest {

  @Test
  public void testGetPackageName() {
    Assertions.assertEquals("X", Sorter.getPackageName("X"));
    Assertions.assertEquals("", Sorter.getPackageName(null));
    Assertions.assertEquals("X", Sorter.getPackageName("a.X"));
    Assertions.assertEquals("X", Sorter.getPackageName("com.a.X"));
  }

  @Test
  public void testSort() {
    List<Version> list = Arrays.asList(new Version(), new Version());
    list.get(0).name = "com.Z";
    list.get(1).name = "org.A";
    Sorter.sortByPackageName(list, (Function<Version, String>) x -> x.name);
    Assertions.assertEquals("org.A", list.get(0).name);
    Assertions.assertEquals("com.Z", list.get(1).name);

    list.get(0).name = "org.A";
    list.get(1).name = "com.Z";
    Sorter.sortByPackageName(list, (Function<Version, String>) x -> x.name);
    Assertions.assertEquals("org.A", list.get(0).name);
    Assertions.assertEquals("com.Z", list.get(1).name);

    list.get(0).name = "Z";
    list.get(1).name = "a";
    Sorter.sortByPackageName(list, (Function<Version, String>) x -> x.name);
    Assertions.assertEquals("a", list.get(0).name);
    Assertions.assertEquals("Z", list.get(1).name);
  }
}
