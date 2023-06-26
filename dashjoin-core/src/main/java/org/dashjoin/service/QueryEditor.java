package org.dashjoin.service;

import java.util.List;
import java.util.Map;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.Table;
import org.dashjoin.util.Escape;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

/**
 * REST API for editing and managing queries
 */
public interface QueryEditor {

  /**
   * delegate REST class which looks up the DB instance from the database request object field
   */
  @Path(Services.REST_PREFIX + "queryeditor")
  @Produces({MediaType.APPLICATION_JSON})
  @Consumes({MediaType.APPLICATION_JSON})
  public static class Delegate implements QueryEditor {

    @Inject
    Services services;

    QueryEditorInternal q(SecurityContext sc, QueryDatabase ac) throws Exception {
      AbstractDatabase db = services.getConfig().getDatabase(ac.database);
      ACLContainerRequestFilter.allowQueryEditor(sc, db);
      if (db == null)
        throw new Exception("Database does not exist. Please select a different table.");
      return db.getQueryEditor();
    }

    QueryResponse limit(QueryResponse res) throws Exception {
      AbstractDatabase db = services.getConfig().getDatabase(res.database);

      for (QueryColumn c : res.metadata)
        if (c.columnID != null) {
          String[] parts = Escape.parseColumnID(c.columnID);
          Table t = db.tables.get(parts[2]);
          if (t != null && t.properties != null)
            c.prop = t.properties.get(parts[3]);
        }

      if (res.limit == null)
        res.limit = 1000;
      while (res.data.size() > res.limit)
        res.data.remove(res.data.size() - 1);
      return res;
    }

    @Override
    public QueryResponse rename(SecurityContext sc, RenameRequest ac) throws Exception {
      return limit(q(sc, ac).rename(ac));
    }

    @Override
    public QueryResponse distinct(SecurityContext sc, DistinctRequest ac) throws Exception {
      return limit(q(sc, ac).distinct(ac));
    }

    @Override
    public QueryResponse sort(SecurityContext sc, SortRequest ac) throws Exception {
      return limit(q(sc, ac).sort(ac));
    }

    @Override
    public QueryResponse addColumn(SecurityContext sc, AddColumnRequest ac) throws Exception {
      return limit(q(sc, ac).addColumn(ac));
    }

    @Override
    public QueryResponse removeColumn(SecurityContext sc, RemoveColumnRequest ac) throws Exception {
      return limit(q(sc, ac).removeColumn(ac));
    }

    @Override
    public QueryResponse setWhere(SecurityContext sc, SetWhereRequest ac) throws Exception {
      return limit(q(sc, ac).setWhere(ac));
    }

    @Override
    public QueryResponse setGroupBy(SecurityContext sc, SetWhereRequest ac) throws Exception {
      return limit(q(sc, ac).setGroupBy(ac));
    }

    @Override
    public QueryResponse moveColumn(SecurityContext sc, MoveColumnRequest ac) throws Exception {
      return limit(q(sc, ac).moveColumn(ac));
    }

    @Override
    public QueryResponse noop(SecurityContext sc, QueryDatabase query) throws Exception {
      return limit(q(sc, query).noop(query));
    }

    @Override
    public QueryResponse getInitialQuery(SecurityContext sc, InitialQueryRequest ac)
        throws Exception {
      Table s = services.getConfig().getSchema(ac.table);
      AbstractDatabase db = services.getConfig().getDatabase(s.parent);

      // we require access to the entire DB since we can join tables
      ACLContainerRequestFilter.allowQueryEditor(sc, db);

      return limit(db.getQueryEditor().getInitialQuery(ac));
    }
  }

  /**
   * represents one column of a tabular query result
   */
  @Schema(title = "QueryColumn: describes a table result column")
  public static class QueryColumn {

    /**
     * property PK
     */
    @Schema(title = "The ID of the property. Null if the column is computed",
        example = "dj/northwind/EMPLOYEES/LAST_NAME")
    public String columnID;

    /**
     * display name of the column (might differ from col name if select COL as DISPAY ... is used)
     */
    @Schema(title = "The projection name. This is also the key in the table row objects")
    public String displayName;

    /**
     * the column projected
     */
    @Schema(title = "The column object describing the column. Null if the column is computed")
    public Col col;

    /**
     * type (e.g. number, string, object, array)
     */
    @Schema(title = "JSON type of the column (number, string, object, array)", example = "number")
    public String type;

    /**
     * The native DB type
     */
    @Schema(title = "Native type in the source database")
    public String dbType;

    /**
     * if the column is a pk, keyTable contains the table name (same as col.table). If it is a fk,
     * contains the name of the referenced table
     */
    @Schema(
        title = "if the column is a pk, keyTable contains the table name (same as col.table). If it is a fk, contains the name of the referenced table")
    public String keyTable;

    /**
     * current database
     */
    @Schema(title = "ID of the database we'Re operating on", example = "dj/northwind")
    public String database;

    /**
     * where clause applied
     */
    @Schema(title = "Filter applied on the column. Null if no filter is present", example = "< 4")
    public String where;

    /**
     * groupBy clause applied
     */
    @Schema(title = "Aggregation applied on the column. Null if no aggregation is present",
        example = "GROUP BY")
    public String groupBy;

    /**
     * optional column schema
     */
    @Schema(title = "Optional column schema")
    public Property prop;
  }

  /**
   * result of the query (data + metadata)
   */
  @Schema(title = "QueryResponse: returned by all query editor calls")
  public static class QueryResponse extends QueryDatabase {

    @Schema(title = "Query result as a list of objects, each representing one row")
    public List<Map<String, Object>> data;

    @Schema(title = "List of column metadata")
    public List<QueryColumn> metadata;

    @Schema(title = "Possible columns that can be joined to the current query")
    public List<AddColumnRequest> joinOptions;

    @Schema(title = "The result table's column names from left to right")
    public List<String> fieldNames;

    @Schema(
        title = "In case the query is correct but its structure is outside of the query editor's support, the reason why it is not supported is listed here. Allows the UI to still display the results but disable editing controls.")
    public String compatibilityError;

    @Schema(title = "Distinct query")
    public Boolean distinct;

    @Schema(title = "limit set in the query, null means no limit")
    public Integer querylimit;
  }

  /**
   * identifies a column
   */
  @Schema(title = "Col: identifies a column")
  public static class Col {

    /**
     * column name
     */
    @Schema(title = "column name")
    public String column;

    /**
     * name of the table (omit quotes, schema and database)
     */
    @Schema(title = "name of the table (omit quotes, schema and database)")
    public String table;

    public static Col col(String table, String column) {
      Col res = new Col();
      res.column = column;
      res.table = table == null ? "" : table;
      return res;
    }

    @Override
    public String toString() {
      if ("".equals(table))
        return column;
      return table + "." + column;
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Col)
        return o.toString().equals(toString());
      else
        return false;
    }
  }

  /**
   * base class for all DB query calls
   */
  @Schema(title = "QueryDatabase: Base class for several request and response structures")
  public static class QueryDatabase {

    /**
     * query to run
     */
    @Schema(title = "The query to edit / the resulting query (in case we're in a response context)")
    public String query;

    /**
     * DB to run it on
     */
    @Schema(title = "ID of the database to operate on", example = "dj/northwind")
    public String database;

    @Schema(
        title = "Limit the result table to 'limit' rows. This value is overridden if the query states the limit explicitly.")
    public Integer limit;
  }

  /**
   * base class for all column requests
   */
  @Schema(title = "Base class for all requests")
  public static abstract class AbstractRequest extends QueryDatabase {

    /**
     * the existing column to operate on
     */
    @Schema(title = "the existing column to operate on")
    public Col col;
  }

  /**
   * add / join a col
   */
  @Schema(title = "AddColumnRequest: instrucuts the editor to add / join a column")
  public static class AddColumnRequest extends AbstractRequest {
    /**
     * the column to add
     */
    @Schema(title = "the column to add")
    public Col add;

    /**
     * the preview data (not used by the backend and only included for JSON compatibility)
     */
    @Schema(
        title = "the preview data (not used by the backend and only included for JSON compatibility)")
    public Object preview;
  }

  /**
   * remove column
   */
  @Schema(title = "RemoveColumnRequest: instructs the editor to remove a column projection")
  public static class RemoveColumnRequest extends AbstractRequest {
    //
  }

  /**
   * change sorting
   */
  @Schema(title = "SortRequest: instructs the editor to sort by a column")
  public static class SortRequest extends AbstractRequest {

    /**
     * sort order (null, asc, desc)
     */
    @Schema(title = "sort order (null - empty string, asc, desc)")
    public String order;
  }

  /**
   * rename col
   */
  @Schema(title = "RenameRequest: instructs the editor to rename a column")
  public static class RenameRequest extends AbstractRequest {
    /**
     * name column name
     */
    @Schema(title = "The new column name")
    public String name;
  }

  /**
   * distinct
   */
  @Schema(title = "DistinctRequest: instructs the editor to toggle distinct and change limit")
  public static class DistinctRequest extends QueryDatabase {
    /**
     * distinct
     */
    @Schema(title = "Distinct value to set")
    public Boolean distinct;

    @Schema(title = "limit to set in the query, null means no limit")
    public Integer querylimit;
  }

  /**
   * condition applied to column
   */
  @Schema(title = "ColCondition: defines a filter condition for a column")
  public static class ColCondition {

    /**
     * column where condition is present
     */
    @Schema(title = "The column to apply the filter on")
    public Col col;

    /**
     * condition string
     */
    @Schema(
        title = "The filter condition in the native query syntax, assuming the column is the left operand. Null if no filter is to be set",
        example = "< 4")
    public String condition;
  }

  /**
   * change where clauses
   */
  @Schema(title = "SetWhereRequest: instructs the editor to set filter for the table projections")
  public static class SetWhereRequest extends QueryDatabase {

    /**
     * The conditional expression to add
     */
    @Schema(title = "One filter definition for each table column")
    public List<ColCondition> cols;
  }

  /**
   * move col
   */
  @Schema(title = "MoveColumnRequest: instructs the editor to move a column projection")
  public static class MoveColumnRequest extends AbstractRequest {

    /**
     * to this position index
     */
    @Schema(title = "The new column position")
    public int position;
  }

  /**
   * rename column
   */
  @POST
  @Path("/rename")
  @Operation(summary = "rename column", hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse rename(@Context SecurityContext sc, RenameRequest ac) throws Exception;

  /**
   * distinct
   */
  @POST
  @Path("/distinct")
  @Operation(summary = "toggle distinct", hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse distinct(@Context SecurityContext sc, DistinctRequest ac) throws Exception;

  /**
   * change sort order
   */
  @POST
  @Path("/sort")
  @Operation(summary = "change sort order", hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse sort(@Context SecurityContext sc, SortRequest ac) throws Exception;

  /**
   * add a column to the query (can be in the same table or a table that needs to be joined)
   */
  @POST
  @Path("/addColumn")
  @Operation(
      summary = "add a column to the query (can be in the same table or a table that needs to be joined)",
      hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse addColumn(@Context SecurityContext sc, AddColumnRequest ac) throws Exception;

  /**
   * Remove a column from the query
   */
  @POST
  @Path("/removeColumn")
  @Operation(summary = "Remove a column from the query", hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse removeColumn(@Context SecurityContext sc, RemoveColumnRequest ac)
      throws Exception;

  /**
   * adds a where clause to the query
   */
  @POST
  @Path("/setWhere")
  @Operation(summary = "adds a where clause to the query", hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse setWhere(@Context SecurityContext sc, SetWhereRequest ac) throws Exception;

  /**
   * set group by operation
   */
  @POST
  @Path("/setGroupBy")
  @Operation(summary = "set group by operation", hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse setGroupBy(@Context SecurityContext sc, SetWhereRequest ac) throws Exception;

  /**
   * move / reorder column in the projection
   */
  @POST
  @Path("/moveColumn")
  @Operation(summary = "move / reorder column in the projection", hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse moveColumn(@Context SecurityContext sc, MoveColumnRequest ac)
      throws Exception;

  /**
   * run the query initially in order to get result and metadata
   */
  @POST
  @Path("/noop")
  @Operation(summary = "run the query initially in order to get result and metadata", hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse noop(@Context SecurityContext sc, QueryDatabase query) throws Exception;

  /**
   * JSON input for getInitialQuery
   */
  @Schema(title = "InitialQueryRequest: JSON input for getInitialQuery")
  public static class InitialQueryRequest {

    @Schema(title = "ID of the table that is the basis for the initial query",
        example = "dj/northwind/EMPLOYEES")
    public String table;

    @Schema(title = "Limits the result to 'limit' rows")
    public Integer limit;
  }

  /**
   * returns the initial query for the database and table
   */
  @POST
  @Path("/getInitialQuery")
  @Operation(summary = "returns the initial query for the database and table", hidden = true)
  @APIResponse(
      description = "Object containing the result of the new query, the result metadata and metadata instructing the client about possible actions")
  public QueryResponse getInitialQuery(@Context SecurityContext sc, InitialQueryRequest ac)
      throws Exception;
}
