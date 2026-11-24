package org.dashjoin.util.gql;

import java.math.BigDecimal;

/**
 * comparing query values with record values: numbers by value (1 = 1.0 = "1" from a JSON key),
 * everything else by its text
 */
class Values {

  static boolean equal(Object a, Object b) {
    if (a == null || b == null)
      return a == b;
    BigDecimal x = number(a);
    BigDecimal y = number(b);
    if (x != null && y != null && (a instanceof Number || b instanceof Number))
      return x.compareTo(y) == 0;
    return a.toString().equals(b.toString());
  }

  /** null if the values cannot be ordered */
  static Integer compare(Object a, Object b) {
    BigDecimal x = number(a);
    BigDecimal y = number(b);
    if (x != null && y != null)
      return x.compareTo(y);
    if (a instanceof Number || b instanceof Number || a instanceof Boolean
        || b instanceof Boolean)
      return null;
    return a.toString().compareTo(b.toString());
  }

  static BigDecimal number(Object o) {
    if (o instanceof Number)
      try {
        return new BigDecimal(o.toString());
      } catch (NumberFormatException e) {
        return null;
      }
    if (o instanceof String && ((String) o).matches("-?[0-9]+(\\.[0-9]+)?"))
      return new BigDecimal((String) o);
    return null;
  }
}
