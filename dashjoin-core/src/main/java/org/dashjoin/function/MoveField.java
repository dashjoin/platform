package org.dashjoin.function;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * $moveField(object, from, to) removes the key "from" from object and inserts it in "to". If to is
 * an object, the removed key is added to it. If to is an array, the removed key is added to each
 * array element (if that is an object)
 */
@SuppressWarnings("rawtypes")
public class MoveField extends AbstractVarArgFunction<Object> {

  @SuppressWarnings("unchecked")
  @Override
  public Object run(List arg) throws Exception {

    if (readOnly)
      throw new Exception(
          "$moveField is deprecated. Please use {'x':1} ~> | $ | {'y':{'x':x}},'x' | instead.");

    Map object = (Map) arg.get(0);
    String from = (String) arg.get(1);
    String to = (String) arg.get(2);
    String newname = (String) arg.get(3);
    if (newname == null || newname.isEmpty())
      newname = from;

    if (object == null || from == null || to == null)
      throw new IllegalArgumentException("Syntax: $moveField(object, 'from', 'to')");

    Object f = object.remove(from);
    Object t = object.get(to);
    if (t instanceof List) {
      List l = (List) t;
      for (Object i : l)
        if (i instanceof Map)
          ((Map) i).put(newname, f);
    }
    if (t instanceof Map) {
      Map m = (Map) t;
      m.put(newname, f);
    }
    return object;
  }

  @Override
  public String getID() {
    return "moveField";
  }

  @Override
  public String getType() {
    return "read";
  }

  @Override
  public List<Class> getArgumentClassList() {
    return Arrays.asList(Object.class, String.class, String.class, String.class);
  }
}
