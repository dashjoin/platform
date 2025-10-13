package org.dashjoin.function;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidateTest {

  static Map<String, Object> typeString = Map.of("type", "string");
  static Map<String, Object> typeNumber = Map.of("type", "number");
  static Map<String, Object> typeArray = Map.of("type", "array");
  static Map<String, Object> typeObject = Map.of("type", "object");
  static Map<String, Object> typeBoolean = Map.of("type", "boolean");
  static Map<String, Object> typeNull = Map.of("type", "null");
  static Map<String, Object> typeStringNull = Map.of("type", List.of("string", "null"));

  static Map<String, Object> xBool = Map.of("properties", Map.of("x", typeBoolean));
  static Map<String, Object> itemsBool = Map.of("items", typeBoolean);
  static Map<String, Object> xBoolR =
      Map.of("properties", Map.of("x", typeBoolean), "required", List.of("x"));

  @Test
  public void testString() throws Exception {
    new Validate().run(Arrays.asList(typeNull, null));
    new Validate().run(List.of(typeString, "test"));
    Assertions.assertThrows(Exception.class, () -> new Validate().run(List.of(typeNull, 123)));
    Assertions.assertThrows(Exception.class, () -> new Validate().run(List.of(typeString, 123)));
    Assertions.assertThrows(Exception.class, () -> new Validate().run(List.of(typeNumber, true)));
    Assertions.assertThrows(Exception.class, () -> new Validate().run(List.of(typeArray, 123)));
    Assertions.assertThrows(Exception.class, () -> new Validate().run(List.of(typeObject, 123)));
    Assertions.assertThrows(Exception.class, () -> new Validate().run(List.of(typeBoolean, 123)));
  }

  @Test
  public void testStringNull() throws Exception {
    new Validate().run(Arrays.asList(typeStringNull, null));
    new Validate().run(List.of(typeStringNull, "test"));
    Assertions.assertThrows(Exception.class,
        () -> new Validate().run(List.of(typeStringNull, 123)));
  }

  @Test
  public void testProps() throws Exception {
    new Validate().run(List.of(xBool, Map.of("x", true)));
    Assertions.assertThrows(Exception.class,
        () -> new Validate().run(List.of(xBool, Map.of("x", 1))));
  }

  @Test
  public void testItems() throws Exception {
    new Validate().run(List.of(itemsBool, List.of(true, false)));
    Assertions.assertThrows(Exception.class,
        () -> new Validate().run(List.of(itemsBool, List.of(true, 1))));
  }

  @Test
  public void testRequired() throws Exception {
    new Validate().run(List.of(xBoolR, Map.of("x", true)));
    Assertions.assertThrows(Exception.class,
        () -> new Validate().run(List.of(xBoolR, Map.of("y", true))));
  }
}
