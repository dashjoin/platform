package org.dashjoin.service.mongodb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonValue;

/**
 * data structure to represent and modify mongo DB queries
 */
public class MongoDBQuery {

  /**
   * query collection
   */
  public String collection;

  /**
   * https://docs.mongodb.com/manual/reference/operator/aggregation/lookup/
   */
  public BsonDocument lookup = new BsonDocument();

  /**
   * https://docs.mongodb.com/manual/reference/operator/aggregation/match/
   */
  public BsonDocument match = new BsonDocument();

  /**
   * https://docs.mongodb.com/manual/reference/operator/aggregation/group/
   */
  public BsonDocument group = new BsonDocument();

  /**
   * https://docs.mongodb.com/manual/reference/operator/aggregation/project/
   */
  public BsonDocument project = new BsonDocument();

  /**
   * https://docs.mongodb.com/manual/reference/operator/aggregation/sort/
   */
  public BsonDocument sort = new BsonDocument();

  /**
   * https://docs.mongodb.com/manual/reference/operator/aggregation/limit/
   */
  public BsonValue limit;

  /**
   * convert the pojo to an actual query string
   */
  @Override
  public String toString() {
    StringBuffer res = new StringBuffer("db.['" + collection + "'].aggregate([");
    if (!lookup.isEmpty())
      res.append(new BsonDocument("$lookup", lookup) + ", ");
    if (!match.isEmpty())
      res.append(new BsonDocument("$match", match) + ", ");
    if (!group.isEmpty())
      res.append(new BsonDocument("$group", group) + ", ");
    if (!project.isEmpty())
      res.append(new BsonDocument("$project", project) + ", ");
    if (!sort.isEmpty())
      res.append(new BsonDocument("$sort", sort) + ", ");
    if (limit != null)
      res.append(new BsonDocument("$limit", limit) + ", ");
    if (res.toString().endsWith(", ")) {
      res.deleteCharAt(res.length() - 1);
      res.deleteCharAt(res.length() - 1);
    }
    res.append("])");
    return res.toString();
  }

  /**
   * parse query string into pojo
   */
  public MongoDBQuery(String query) {

    // cut off db.
    query = query.trim().substring(3);

    // parse the collection from the query string
    if (query.startsWith("[")) {
      collection = query.substring(1, query.indexOf(']'));
      if (collection.startsWith("'") || collection.startsWith("\""))
        collection = collection.substring(1, collection.length() - 1);
    } else
      collection = query.substring(0, query.indexOf('.'));

    if (query.startsWith(collection + ".find("))
      throw new IllegalArgumentException("Only aggregate queries are supported");

    int start = query.indexOf(".aggregate(") + ".aggregate(".length();
    int end = query.length() - 1;
    String rest = query.substring(start, end).trim();
    if (rest.isEmpty())
      return;
    BsonArray array = BsonArray.parse(rest);
    for (BsonValue i : array) {
      BsonDocument d = (BsonDocument) i;
      if (!d.isEmpty()) {
        Entry<String, BsonValue> entry = d.entrySet().iterator().next();
        if (entry.getKey().equals("$lookup"))
          this.lookup = (BsonDocument) entry.getValue();
        if (entry.getKey().equals("$sort"))
          this.sort = (BsonDocument) entry.getValue();
        if (entry.getKey().equals("$group"))
          this.group = (BsonDocument) entry.getValue();
        if (entry.getKey().equals("$match"))
          this.match = (BsonDocument) entry.getValue();
        if (entry.getKey().equals("$limit"))
          this.limit = entry.getValue();
        if (entry.getKey().equals("$project"))
          this.project = (BsonDocument) entry.getValue();
      }
    }
  }

  /**
   * order by the given column
   */
  public void orderBy(String column, String orderString) {
    if (orderString == null || orderString.isEmpty()) {
      sort = new BsonDocument();
      return;
    }

    sort = new BsonDocument().append(column,
        orderString.equals("asc") ? new BsonInt32(1) : new BsonInt32(-1));
  }

  /**
   * convert the query pojo into a BSON query array that can be used with the query API
   */
  public List<BsonDocument> array() {
    List<BsonDocument> res = new ArrayList<>();
    if (!lookup.isEmpty())
      res.add(new BsonDocument("$lookup", lookup));
    if (!match.isEmpty())
      res.add(new BsonDocument("$match", match));
    if (!group.isEmpty())
      res.add(new BsonDocument("$group", group));
    if (!project.isEmpty())
      res.add(new BsonDocument("$project", project));
    if (!sort.isEmpty())
      res.add(new BsonDocument("$sort", sort));
    if (limit != null)
      res.add(new BsonDocument("$limit", limit));
    return res;
  }
}
