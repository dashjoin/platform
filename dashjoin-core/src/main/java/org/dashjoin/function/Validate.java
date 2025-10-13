package org.dashjoin.function;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Validate extends AbstractVarArgFunction<Void> {

  static ObjectMapper om = new ObjectMapper();

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Void run(List arg) throws Exception {
    Map<String, Object> schema = (Map<String, Object>) arg.get(0);
    Object object = arg.get(1);
    validate(om.convertValue(schema, Schema.class), object);
    return null;
  }

  void type(String type, Object object) {
    if (type.equals("null") && !(object == null))
      error(object + " expected to be of type string");
    if (type.equals("string") && !(object instanceof String))
      error(object + " expected to be of type string");
    if (type.equals("number") && !(object instanceof Number))
      error(object + " expected to be of type number");
    if (type.equals("boolean") && !(object instanceof Boolean))
      error(object + " expected to be of type boolean");
    if (type.equals("array"))
      array(object);
    if (type.equals("object"))
      object(object);
  }

  void validate(Schema schema, Object object) {
    if (schema.type instanceof String)
      type((String) schema.type, object);
    if (schema.type instanceof List) {
      boolean fail = true;
      for (Object type : array(schema.type)) {
        try {
          type((String) type, object);
          fail = false;
        } catch (Exception e) {
        }
      }
      if (fail)
        error(object + " expected to be of type " + schema.type);
    }
    if (schema.properties != null) {
      for (Entry<String, Schema> e : schema.properties.entrySet()) {
        validate(e.getValue(), object(object).get(e.getKey()));
      }
    }
    if (schema.items != null) {
      for (Object item : array(object))
        validate(schema.items, item);
    }
    if (schema.required != null) {
      for (String r : schema.required)
        if (!object(object).containsKey(r))
          error(r + " is reuqired");
    }
  }

  @SuppressWarnings({"unchecked"})
  Map<String, Object> object(Object object) {
    if (!(object instanceof Map))
      error(object + " expected to be of type object");
    return (Map<String, Object>) object;
  }

  @SuppressWarnings({"unchecked"})
  List<Object> array(Object object) {
    if (!(object instanceof List))
      error(object + " expected to be of type array");
    return (List<Object>) object;
  }

  void error(String s) {
    throw new IllegalArgumentException(s);
  }

  @Override
  public String getSignature() {
    return "<ox>";
  }

  static class Schema {
    public Object type;
    public Map<String, Schema> properties;
    public Schema items;
    public List<String> required;
  }
}
