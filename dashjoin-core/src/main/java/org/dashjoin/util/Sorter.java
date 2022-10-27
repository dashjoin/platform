package org.dashjoin.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.dashjoin.function.Email;
import org.dashjoin.function.Invoke;
import org.dashjoin.function.RestJson;
import org.dashjoin.mapping.Receive;
import org.dashjoin.service.PojoDatabase;
import org.dashjoin.service.RemoteDatabase;
import org.dashjoin.service.SQLDatabase;

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

  static List<String> core = Arrays.asList(SQLDatabase.class.getName(),
      PojoDatabase.class.getName(), RemoteDatabase.class.getName(), Invoke.class.getName(),
      Receive.class.getName(), Email.class.getName(), RestJson.class.getName());

  public static <T> List<T> sortImplementations(Iterable<T> iter) {
    List<T> one = new ArrayList<>();
    List<T> two = new ArrayList<>();
    for (T t : iter)
      if (core.contains(t.getClass().getName()))
        one.add(t);
      else
        two.add(t);
    one.addAll(two);
    return one;
  }
}
