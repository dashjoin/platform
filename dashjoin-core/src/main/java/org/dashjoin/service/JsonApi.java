package org.dashjoin.service;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.util.MapUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Endpoint that exposes the dashjoin API as JSON:API (https://jsonapi.org/)
 */
@Path(Services.REST_PREFIX + "jsonapi")
@Produces({"application/vnd.api+json"})
@Consumes({"application/vnd.api+json"})
public class JsonApi {

  private static final ObjectMapper om = new ObjectMapper();

  @Inject
  Data data;

  @Inject
  Services services;

  /**
   * wraps common functionality for generating json:api links etc.
   */
  class Call {

    /**
     * base URI so we can generate absolute URLs
     */
    UriInfo uriInfo;

    /**
     * database we operate on
     */
    String database;

    /**
     * type / table
     */
    String type;

    /**
     * init type and database
     */
    public Call(UriInfo uriInfo, String database, String type) {
      this.uriInfo = uriInfo;
      this.database = database;
      this.type = type;
    }

    /**
     * lookup the Table object containing the metadata
     */
    Table table() throws Exception {
      AbstractDatabase db = services.getConfig().getDatabase(data.dj(database));
      return db.tables.get(type);
    }

    /**
     * generate a link object (https://jsonapi.org/format/#document-links)
     */
    Map<String, Object> self(String id) {
      String pars = fields();
      return MapUtil.of("self", uriInfo.getBaseUri() + "rest/jsonapi/" + database + "/" + type + "/"
          + id + (pars.isEmpty() ? "" : "?") + pars);
    }

    /**
     * generate a link object (https://jsonapi.org/format/#document-links)
     */
    Map<String, String> self(Integer offset, Integer limit, String filter, String sort,
        Integer size) {
      String o = offset == null ? "" : "page%5Boffset%5D=" + offset + "&";
      String l = limit == null ? "" : "page%5Blimit%5D=" + limit + "&";
      String s = sort == null ? "" : "sort=" + e(sort) + "&";
      String f = filter == null ? "" : "filter=" + e(filter) + "&";
      String pars = o + l + f + s + fields();
      if (pars.endsWith("&"))
        pars = pars.substring(0, pars.length() - 1);
      Map<String, String> self = MapUtil.of("self", uriInfo.getBaseUri() + "rest/jsonapi/"
          + database + "/" + type + (pars.isEmpty() ? "" : "?") + pars);

      if (size != null && size >= limit)
        self.put("next",
            self(offset == null ? limit : offset + limit, limit, filter, sort, null).get("self"));

      return self;
    }

    /**
     * generate the fields[type]=a,b query parameters
     */
    String fields() {
      List<String> fs = new ArrayList<>();
      for (Entry<String, List<String>> x : uriInfo.getQueryParameters().entrySet())
        if (x.getKey().startsWith("fields["))
          if (x.getValue() != null)
            fs.add(e(x.getKey()) + "=" + x.getValue().get(0));
      return String.join("&", fs);
    }

    /**
     * URLencode
     */
    String e(String s) {
      return URLEncoder.encode(s, Charset.defaultCharset());
    }

    /**
     * create a resource object (https://jsonapi.org/format/#document-resource-objects)
     */
    Map<String, Object> convert(Map<String, Object> o) throws Exception {
      String id = null;
      Table t = table();
      Map<String, Object> ref = new LinkedHashMap<>();

      for (Property p : t.properties.values()) {
        if (p.pkpos != null && p.pkpos == 0)
          id = "" + o.remove(p.name);
        List<String> fields = uriInfo.getQueryParameters().get("fields[" + type + "]");
        if (fields != null && fields.size() > 0
            && !Arrays.asList(fields.get(0).split(",")).contains(p.name))
          o.remove(p.name);
        if (p.ref != null && o.get(p.name) != null) {
          Call x = new Call(uriInfo, p.ref.split("/")[1], p.ref.split("/")[2]);
          ref.put(p.name,
              MapUtil.of("links", MapUtil.of("related", x.self("" + o.get(p.name)).get("self"))));
        }
      }

      if (ref.isEmpty())
        return MapUtil.of("id", id, "type", t.name, "attributes", o);
      else
        return MapUtil.of("id", id, "type", t.name, "attributes", o, "relationships", ref);
    }
  }

  /**
   * discover types
   */
  @GET
  @Path("/{database}")
  public Map<String, Object> discover(@Context UriInfo uriInfo, @Context SecurityContext sc,
      @PathParam("database") String database) throws Exception {

    List<String> res = new ArrayList<>();
    for (String d : data.tables())
      if (d.startsWith("dj/" + database + "/"))
        res.add(uriInfo.getBaseUri() + "rest/jsonapi/" + d.substring("dj/".length()));
    return MapUtil.of("links",
        MapUtil.of("self", uriInfo.getBaseUri() + "rest/jsonapi/" + database), "meta",
        MapUtil.of("types", res));
  }

  /**
   * get a single record
   */
  @GET
  @Path("/{database}/{type}/{id}")
  public Map<String, Object> getRecord(@Context UriInfo uriInfo, @Context SecurityContext sc,
      @PathParam("database") String database, @PathParam("type") String type,
      @PathParam("id") String id) throws Exception {

    Call call = new Call(uriInfo, database, type);
    Map<String, Object> res = data.read(sc, database, type, id);
    return MapUtil.of("links", call.self(id), "data", call.convert(res));
  }

  /**
   * get records of a given type
   */
  @GET
  @Path("/{database}/{type}")
  public Map<String, Object> getTable(@Context UriInfo uriInfo, @Context SecurityContext sc,
      @PathParam("database") String database, @PathParam("type") String type,
      @QueryParam("sort") String sort, @QueryParam("filter") String filter,
      @QueryParam("page[offset]") Integer offset, @QueryParam("page[limit]") Integer limit)
      throws Exception {

    // filter
    Map<String, Object> arguments = null;
    if (filter != null)
      try {
        arguments = om.readValue(filter, JSONDatabase.tr);
      } catch (JsonProcessingException e) {
        throw new Exception(
            "Error parsing filter: " + filter + ". Please provide a valid JSON filter object.");
      }

    // sort
    boolean descending = false;
    String s = sort;
    if (sort != null) {
      if (sort.contains(","))
        throw new Exception("Sorting by multiple fields is not supported");
      if (sort.startsWith("-")) {
        descending = true;
        s = sort.substring(1);
      }
    }

    // limit
    limit = limit == null ? 1000 : limit;

    Call call = new Call(uriInfo, database, type);
    List<Map<String, Object>> res = new ArrayList<>();
    List<Map<String, Object>> all =
        data.all(sc, database, type, offset, limit, s, descending, arguments);
    for (Map<String, Object> i : all)
      res.add(call.convert(i));
    return MapUtil.of("links", call.self(offset, limit, filter, sort, all.size()), "data", res);
  }
}
