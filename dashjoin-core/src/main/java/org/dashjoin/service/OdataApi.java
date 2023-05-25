package org.dashjoin.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.dashjoin.util.MapUtil;

/**
 * Endpoint that exposes the dashjoin API as ODATA (https://www.odata.org/)
 */
@Path(Services.REST_PREFIX + "odata")
@Produces({"application/json"})
@Consumes({"application/json"})
public class OdataApi {

  @Inject
  Data data;

  @Inject
  Services services;

  @GET
  @Path("/{database}/{type}/{field}/$value")
  public Object getValue(@Context UriInfo uriInfo, @Context SecurityContext sc,
      @PathParam("database") String database, @PathParam("type") String type,
      @PathParam("field") String field, @QueryParam("$filter") String filter,
      @QueryParam("$select") String select, @QueryParam("$orderby") String orderby,
      @QueryParam("$top") Integer top, @QueryParam("$skip") Integer skip) throws Exception {

    @SuppressWarnings("unchecked")
    Map<String, Object> res = (Map<String, Object>) getTable(uriInfo, sc, database, type, filter,
        select, orderby, top, skip).get("value");
    return res.get(field);
  }

  @GET
  @Path("/{database}/{type}/{field}")
  public Map<String, Object> getField(@Context UriInfo uriInfo, @Context SecurityContext sc,
      @PathParam("database") String database, @PathParam("type") String type,
      @PathParam("field") String field, @QueryParam("$filter") String filter,
      @QueryParam("$select") String select, @QueryParam("$orderby") String orderby,
      @QueryParam("$top") Integer top, @QueryParam("$skip") Integer skip) throws Exception {

    return MapUtil.of("value",
        getValue(uriInfo, sc, database, type, field, filter, select, orderby, top, skip));
  }

  @GET
  @Path("/{database}/{type}")
  public Map<String, Object> getTable(@Context UriInfo uriInfo, @Context SecurityContext sc,
      @PathParam("database") String database, @PathParam("type") String type,
      @QueryParam("$filter") String filter, @QueryParam("$select") String select,
      @QueryParam("$orderby") String orderby, @QueryParam("$top") Integer top,
      @QueryParam("$skip") Integer skip) throws Exception {

    if (type.contains("(") && type.endsWith(")")) {
      String table = type.split("\\(")[0];
      String id = type.split("\\(")[1];
      id = id.substring(0, id.length() - 1);
      if (id.startsWith("'") && id.endsWith("'"))
        id = id.substring(1, id.length() - 1);
      Map<String, Object> res = data.read(sc, database, table, Arrays.asList(id));
      return MapUtil.of("value", res);
    } else {
      // filter
      Map<String, Object> args = MapUtil.of();
      if (filter != null)
        for (String con : filter.split(" and ")) {
          String[] parts = con.split(" eq ");
          args.put(parts[0].trim(), parse(parts[1].trim()));
        }

      // sort
      String sort = orderby;
      boolean descending = false;
      if (sort != null) {
        if (sort.endsWith(" desc")) {
          descending = true;
          sort = sort.substring(0, sort.length() - " desc".length());
        }
      }

      List<Map<String, Object>> res =
          data.all(sc, database, type, skip, top, sort, descending, args);
      if (select != null) {
        String[] sel = select.split(",");
        List<Map<String, Object>> ress = new ArrayList<>();
        for (Map<String, Object> row : res) {
          Map<String, Object> newrow = MapUtil.of();
          for (String s : sel)
            newrow.put(s.trim(), row.get(s.trim()));
          ress.add(newrow);
        }
        res = ress;
      }

      return MapUtil.of("value", res);
    }
  }

  /**
   * parse filter literal
   */
  Object parse(String s) throws Exception {
    if (s.equals("true"))
      return true;
    if (s.equals("false"))
      return false;
    if (s.startsWith("'") && s.endsWith("'"))
      return s.substring(1, s.length() - 1);
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      try {
        return Double.parseDouble(s);
      } catch (NumberFormatException e1) {
        throw new Exception("Cannot parse: " + s);
      }
    }
  }
}
