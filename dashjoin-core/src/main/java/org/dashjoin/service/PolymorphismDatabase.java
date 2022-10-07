package org.dashjoin.service;

import static com.google.common.collect.ImmutableMap.of;
import static com.google.common.collect.Maps.newHashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.dashjoin.function.AbstractConfigurableFunction;
import org.dashjoin.function.Function;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.JsonSchema;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

/**
 * DB that presents database json schema annotations to the config DB
 */
public class PolymorphismDatabase extends JSONDatabase {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public Map<String, Map<String, Object>> queryMap(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    if (info.query.equals("dj-database")) {
      return of("dj/config", tableProperties());
    }
    return of();
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    if (s.name.equals("dj-database"))
      if (search.get("ID").equals("dj/config"))
        return tableProperties();
    return null;
  }

  /**
   * load data for all DB implementations
   */
  Map<String, Object> tableProperties() {
    return of("tables", of("dj-database", tableProperties(Database.class, "dj-database"),
        "dj-function", tableProperties(Function.class, "dj-function")));
  }

  @SuppressWarnings("unchecked")
  Map<String, Object> tableProperties(Class<?> clazz, String tableName) {
    Map<String, Object> res = null;
    Set<Class<?>> inheritance = new HashSet<>();
    for (Object db : SafeServiceLoader.load(clazz)) {

      // skip functions that are not configurable functions
      if (clazz.equals(Function.class))
        if (!(db instanceof AbstractConfigurableFunction))
          continue;

      // merge arrays (to preserve case array if field appears in multiple implementations)
      Map<String, Object> map = jsonSchema(db.getClass());

      // pick up top level class annotation (since there is no field variable holding it)
      List<JsonSchema> classes = new ArrayList<>();
      Class<?> current = db.getClass();
      while (current != null) {
        if (current.getAnnotation(JsonSchema.class) != null)
          if (inheritance.add(current))
            classes.add(current.getAnnotation(JsonSchema.class));
        current = current.getSuperclass();
      }
      classes = Lists.reverse(classes);
      for (JsonSchema s : classes) {
        Map<String, Object> tmp = new HashMap<>();
        put(tmp, s);
        map = UnionDatabase.mergeArray(map, tmp);
      }
      res = UnionDatabase.mergeArray(res, map);
    }

    res.put("ID", "dj/config/" + tableName);
    res.put("name", tableName);
    res.put("parent", "dj/config");

    // remove duplicates from order in case several implementations use the same field
    Object order = res.get("order");
    if (order instanceof List) {
      List<Object> distinct = new ArrayList<>(new LinkedHashSet<String>((List<String>) order));
      int djClassName = distinct.indexOf("djClassName");
      if (djClassName >= 0) {
        String nameOrID = tableName.equals("dj-database") ? "name" : "ID";
        distinct.set(djClassName,
            distinct.remove("title")
                ? Arrays.asList("djClassName", nameOrID, "comment", "title")
                : Arrays.asList("djClassName", nameOrID, "comment"));
        distinct.remove(nameOrID);
        distinct.remove("comment");
      }
      int roles = distinct.indexOf("roles");
      if (roles >= 0) {
        distinct.set(roles, Arrays.asList("roles", "type"));
        distinct.remove("type");
      }
      int oldData = distinct.indexOf("oldData");
      if (oldData >= 0) {
        distinct.set(oldData, Arrays.asList("oldData", "createSchema"));
        distinct.remove("createSchema");
      }
      int readRoles = distinct.indexOf("readRoles");
      if (readRoles >= 0) {
        distinct.set(readRoles, Arrays.asList("readRoles", "writeRoles"));
        distinct.remove("writeRoles");
      }
      int username = distinct.indexOf("username");
      int password = distinct.indexOf("password");
      if (username >= 0 && password >= 0) {
        distinct.set(username, Arrays.asList("username", "password"));
        distinct.remove("password");
      }
      int expression = distinct.indexOf("expression");
      int expressions = distinct.indexOf("expressions");
      if (expression >= 0 && expressions >= 0) {
        distinct.remove("expressions");
        expression = distinct.indexOf("expression");
        distinct.add(expression, "expressions");
      }
      res.put("order", distinct);
    }

    // remove duplicates from enum list
    for (Object i : ((Map<String, Object>) res.get("properties")).values()) {
      Map<String, Object> typeProp = (Map<String, Object>) i;
      if (typeProp != null && typeProp.get("enum") instanceof List<?>) {
        Object s = new LinkedHashSet<>((List<String>) typeProp.get("enum"));
        typeProp.put("enum", s);
      }
    }

    return res;
  }

  /**
   * generates a JSON schema document from the given pojo class. Note that the class can also be an
   * array, e.g. Person[]. The Class.forName syntax is:
   * Class.forName("[Lcom.x.db.jdbc.DBObjectTest$Person;"
   */
  public static Map<String, Object> jsonSchema(Class<?> c) {
    if (c.isArray())
      return array(c.getComponentType());
    else if (Map.class.isAssignableFrom(c))
      return of("type", "object", "additionalProperties", of("type", "string"));
    else
      return object(c);
  }

  /**
   * json schema for object
   */
  static Map<String, Object> object(Class<?> c) {
    Map<String, Object> res = new LinkedHashMap<>();
    Map<String, Object> properties = new LinkedHashMap<>();
    res.put("type", "object");
    res.put("properties", properties);

    if (AbstractDatabase.class.isAssignableFrom(c))
      res.put("switch", "djClassName");
    if (AbstractConfigurableFunction.class.isAssignableFrom(c))
      res.put("switch", "djClassName");

    for (Field f : c.getFields()) {
      if (Modifier.isStatic(f.getModifiers()))
        continue;
      if (f.getAnnotation(JsonIgnore.class) != null)
        continue;
      Map<String, Object> x =
          item(f.getAnnotation(JsonSchema.class), f.getType(), f.getGenericType());
      if ((!f.getDeclaringClass().equals(AbstractDatabase.class))
          && (!f.getDeclaringClass().equals(AbstractConfigurableFunction.class)))
        x.put("case", Lists.newArrayList(c.getName()));
      if (AbstractDatabase.class.isAssignableFrom(c))
        x.put("ID", "dj/config/dj-database/" + f.getName());
      if (AbstractConfigurableFunction.class.isAssignableFrom(c))
        x.put("ID", "dj/config/dj-function/" + f.getName());
      properties.put(f.getName(), x);
    }
    return res;
  }

  static void put(Map<String, Object> res, String key, boolean value) {
    if (value)
      res.put(key, value);
  }

  static void put(Map<String, Object> res, String key, String value) {
    if (value != null && !value.isEmpty())
      res.put(key, value);
  }

  static void put(Map<String, Object> res, String key, String[] value) {
    if (value != null && value.length != 0)
      res.put(key, new ArrayList<>(Arrays.asList(value)));
  }

  static void put(Map<String, Object> res, JsonSchema s) {
    if (s != null) {
      put(res, "choicesUrl", s.choicesUrl());
      put(res, "choicesVerb", s.choicesVerb());
      put(res, "description", s.description());
      put(res, "jsonata", s.jsonata());
      put(res, "displayWith", s.displayWith());
      put(res, "choices", s.choices());
      put(res, "readOnly", s.readOnly());
      put(res, "createOnly", s.createOnly());
      put(res, "layout", s.layout());
      put(res, "title", s.title());
      put(res, "widget", s.widget());
      put(res, "widgetType", s.widgetType());
      if (s.style().length > 0) {
        Map<String, Object> style = new HashMap<>();
        for (int i = 0; i < s.style().length; i = i + 2)
          style.put(s.style()[i], s.style()[i + 1]);
        res.put("style", style);
      }
      if (s.order().length > 0)
        put(res, "order", s.order());
      if (s.enums().length > 0)
        put(res, "enum", s.enums());
      if (s.required().length > 0)
        put(res, "required", s.required());
      if (s.computed() != null && !s.computed().isEmpty()) {
        try {
          res.put("computed", objectMapper.readValue(s.computed(), JSONDatabase.tr));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  /**
   * schema for a pojo
   */
  static Map<String, Object> item(JsonSchema s, Class<?> c, Type genericType) {
    Map<String, Object> res = new LinkedHashMap<>();

    put(res, s);

    if (c.isArray()) {
      res.putAll(array(c.getComponentType()));
    } else if (Collection.class.isAssignableFrom(c)) {
      if (genericType instanceof ParameterizedType)
        res.putAll(array((Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0]));
      else
        res.putAll(array(String.class));
    } else if (Map.class.isAssignableFrom(c)) {
      res.put("type", "object");
      res.put("additionalProperties", newHashMap(of("type", "string")));
    } else if (Number.class.isAssignableFrom(c))
      res.put("type", "number");
    else if (Boolean.class.equals(c))
      res.put("type", "boolean");
    else if (String.class.equals(c))
      res.put("type", "string");
    else
      res.putAll(object(c));
    return res;
  }

  static Map<String, Object> array(Class<?> c) {
    Map<String, Object> res = new LinkedHashMap<>();
    res.put("type", "array");
    res.put("items", item(null, c, null));
    return res;
  }
}
