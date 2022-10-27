package org.dashjoin.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * sorting utilities
 */
public class Sorter {

  public static <T> List<T> sortByPackageName(List<T> res, Function<T, String> f) {
    Collections.sort(res, new Comparator<T>() {

      @Override
      public int compare(T o1, T o2) {
        String s1 = getPackageName(f.apply(o1));
        String s2 = getPackageName(f.apply(o2));
        return s1.toLowerCase().compareTo(s2.toLowerCase());
      }

    });
    return res;
  }

  public static String getPackageName(String s) {
    if (s == null)
      s = "";
    String[] a = s.split("\\.");
    return a[a.length - 1];
  }
}
