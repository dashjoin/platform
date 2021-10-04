package org.dashjoin.service;

import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.dashjoin.model.AbstractDatabase;
import org.dashjoin.model.Property;
import org.dashjoin.model.QueryMeta;
import org.dashjoin.model.Table;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import com.google.common.net.UrlEscapers;

/**
 * RDF4J implementation
 */
public class RDF4J extends AbstractDatabase {

  public List<String> datasets;

  /**
   * shared connection pool
   */
  Repository _cp;

  /**
   * value factory tied to the repo
   */
  ValueFactory vf;

  RepositoryConnection getConnection() throws RepositoryException {
    try {
      RDF4J x = services.getConfig().getCachedForce(ID, getClass());
      if (x._cp == null)
        throw new Exception("Database not yet initialized: " + ID);
      vf = x.vf;
      return x._cp.getConnection();
    } catch (RepositoryException e) {
      throw e;
    } catch (Exception e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public void close() {

  }

  Object object(Value object) {
    if (object instanceof Literal) {
      Literal literal = (Literal) object;
      if (XMLSchema.INTEGER.equals(literal.getDatatype()))
        return literal.intValue();
      if (XMLSchema.INT.equals(literal.getDatatype()))
        return literal.intValue();
      if (XMLSchema.STRING.equals(literal.getDatatype()))
        return literal.stringValue();
      if (RDF.LANGSTRING.equals(literal.getDatatype()))
        return literal.stringValue();
      if (XMLSchema.BOOLEAN.equals(literal.getDatatype()))
        return literal.booleanValue();
      if (XMLSchema.DOUBLE.equals(literal.getDatatype()))
        return literal.doubleValue();
      if (XMLSchema.DECIMAL.equals(literal.getDatatype()))
        return literal.decimalValue();
      if (XMLSchema.DATE.equals(literal.getDatatype()))
        return literal.calendarValue();
      if (XMLSchema.GYEAR.equals(literal.getDatatype()))
        return literal.calendarValue();
      if (XMLSchema.DATETIME.equals(literal.getDatatype()))
        return literal.calendarValue();
      throw new RuntimeException("Not implemented");
    } else
      return string((Resource) object);
  }

  IRI iri(Table t) {
    return iri(URLDecoder.decode(t.name, StandardCharsets.UTF_8));
  }

  IRI iri(Object o) {
    String s = "" + o;
    if (vf == null)
      vf = SimpleValueFactory.getInstance();
    return vf.createIRI(s);
  }

  String string(Resource r) {
    return r.stringValue();
  }

  Literal literal(Object o) {
    if (o instanceof String)
      return vf.createLiteral((String) o);
    if (o instanceof Integer)
      return vf.createLiteral((Integer) o);
    if (o instanceof Long)
      return vf.createLiteral((Long) o);
    if (o instanceof Boolean)
      return vf.createLiteral((Boolean) o);
    if (o instanceof Date)
      return vf.createLiteral((Date) o);
    if (o instanceof Float)
      return vf.createLiteral((Float) o);
    if (o instanceof Double)
      return vf.createLiteral((Double) o);
    throw new RuntimeException("Not implemented");
  }

  @Override
  public List<Map<String, Object>> query(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    try (RepositoryConnection con = getConnection()) {
      List<Map<String, Object>> res = new ArrayList<>();
      TupleQuery tq = con.prepareTupleQuery(info.query);
      try (TupleQueryResult i = tq.evaluate()) {
        while (i.hasNext()) {
          BindingSet x = i.next();
          Map<String, Object> row = new LinkedHashMap<>();
          res.add(row);
          x.forEach(b -> row.put(b.getName(), object(b.getValue())));
        }
      }
      return res;
    }
  }

  @Override
  public void create(Table m, Map<String, Object> object) throws Exception {
    try (RepositoryConnection con = getConnection()) {
      IRI subject = iri(object.get("ID"));
      con.add(subject, RDF.TYPE, iri(m));
      for (Entry<String, Object> entry : object.entrySet()) {
        if (!entry.getKey().equals("ID")) {
          con.add(subject, iri(entry.getKey()), literal(entry.getValue()));
        }
      }
    }
  }

  @Override
  public Map<String, Object> read(Table s, Map<String, Object> search) throws Exception {
    try (RepositoryConnection con = getConnection()) {
      try (RepositoryResult<Statement> i = con.getStatements(iri(search.get("ID")), null, null)) {
        Map<String, Object> res = new HashMap<>();
        boolean found = false;
        while (i.hasNext()) {
          found = true;
          Statement x = i.next();
          if (!x.getPredicate().equals(RDF.TYPE))
            res.put(string(x.getPredicate()), object(x.getObject()));
        }
        if (found) {
          res.put("ID", search.get("ID"));
          return res;
        } else
          return null;
      }
    }
  }

  @Override
  public boolean update(Table schema, Map<String, Object> search, Map<String, Object> object)
      throws Exception {
    try (RepositoryConnection con = getConnection()) {
      IRI subject = iri(search.get("ID"));
      try (RepositoryResult<Statement> types = con.getStatements(subject, RDF.TYPE, iri(schema))) {
        if (types.hasNext()) {
          for (Entry<String, Object> entry : object.entrySet()) {
            if (!entry.getKey().equals("ID")) {
              if (entry.getValue() == null)
                con.remove(subject, iri(entry.getKey()), literal(entry.getValue()));
              else
                con.add(subject, iri(entry.getKey()), literal(entry.getValue()));
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean delete(Table s, Map<String, Object> search) throws Exception {
    try (RepositoryConnection con = getConnection()) {
      IRI subject = iri(search.get("ID"));
      try (RepositoryResult<Statement> types = con.getStatements(subject, null, null)) {
        if (!types.hasNext())
          return false;
        while (types.hasNext()) {
          con.remove(types.next());
        }
        return true;
      }
    }
  }

  @Override
  public List<String> getTablesInQuery(String query) throws Exception {
    throw new RuntimeException();
  }

  @Override
  public QueryEditorInternal getQueryEditor() {
    return new RDF4JEditor(services, this);
  }

  @Override
  public List<Map<String, Object>> all(Table s, Integer offset, Integer limit, String sort,
      boolean descending, Map<String, Object> arguments) throws Exception {
    String l = limit == null ? "" : " limit " + limit;
    String o = offset == null ? "" : " offset" + offset;
    try (RepositoryConnection con = getConnection()) {
      Map<String, Map<String, Object>> table = new HashMap<>();
      TupleQuery tq =
          con.prepareTupleQuery("select ?s ?p ?o where {?s a <" + iri(s) + "> . ?s ?p ?o}" + l + o);
      try (TupleQueryResult i = tq.evaluate()) {
        while (i.hasNext()) {
          BindingSet x = i.next();
          Resource subject = (Resource) x.getBinding("s").getValue();
          IRI predicate = (IRI) x.getBinding("p").getValue();
          Value object = x.getBinding("o").getValue();
          if (!predicate.equals(RDF.TYPE))
            getRow(table, string(subject)).put(string(predicate), object(object));
        }
      }
      return new ArrayList<>(table.values());
    }
  }

  Map<String, Object> getRow(Map<String, Map<String, Object>> table, String subject) {
    Map<String, Object> res = table.get(subject);
    if (res == null) {
      res = new HashMap<>();
      res.put("ID", subject);
      table.put(subject, res);
    }
    return res;
  }

  @Override
  public Map<String, Property> queryMeta(QueryMeta info, Map<String, Object> arguments)
      throws Exception {
    try (RepositoryConnection con = getConnection()) {
      Map<String, Property> res = new HashMap<>();
      TupleQuery tq = con.prepareTupleQuery(info.query);
      try (TupleQueryResult i = tq.evaluate()) {
        while (i.hasNext()) {
          BindingSet x = i.next();
          x.forEach(b -> {
            Property prop = new Property();
            prop.pkpos = b.getValue() instanceof IRI ? 0 : null;
            prop.name = b.getName();
            if (b.getValue() instanceof IRI) {
              IRI type = getType((Resource) b.getValue());
              if (type != null)
                prop.parent = type.stringValue();
            }
            res.put(b.getName(), prop);
          });
          return res;
        }
      }
    }
    return null;
  }

  @Override
  public Map<String, Object> connectAndCollectMetadata() throws Exception {
    Sail sail = new MemoryStore();
    _cp = new SailRepository(sail);
    _cp.init();
    vf = _cp.getValueFactory();

    Map<String, Object> meta = new HashMap<>();

    try (RepositoryConnection con = _cp.getConnection()) {

      if (datasets != null)
        for (String s : datasets) {
          InputStream ddl = getClass().getResourceAsStream(s);
          RDFFormat format = Rio.getParserFormatForFileName(s).orElse(RDFFormat.RDFXML);
          con.add(ddl, "", format);
        }

      for (IRI[] i : new IRI[][] {new IRI[] {RDF.TYPE, OWL.CLASS}, new IRI[] {RDF.TYPE, RDFS.CLASS},
          new IRI[] {RDFS.SUBCLASSOF, null}})
        try (RepositoryResult<Statement> types = con.getStatements(null, i[0], i[1])) {
          while (types.hasNext()) {
            Resource s = types.next().getSubject();
            if (s instanceof IRI) {
              String encoded = UrlEscapers.urlPathSegmentEscaper().escape(s.stringValue());
              Map<String, Object> table = new HashMap<>();
              table.put("parent", ID);
              table.put("name", encoded);
              table.put("ID", ID + "/" + encoded);
              table.put("type", "object");
              Map<String, Object> properties = new HashMap<>();
              Map<String, Object> id = new HashMap<>();
              id.put("pkpos", 0);
              id.put("name", "ID");
              id.put("type", "string");
              id.put("parent", table.get("ID"));
              id.put("ID", table.get("ID") + "/ID");
              properties.put("ID", id);
              table.put("properties", properties);
              meta.put(encoded, table);
            }
          }
        }
    }
    return meta;
  }

  public IRI getType(Resource iri) {
    try (RepositoryConnection con = getConnection()) {
      try (RepositoryResult<Statement> out = con.getStatements(iri, RDF.TYPE, null)) {
        while (out.hasNext()) {
          Value s = out.next().getObject();
          if (s instanceof IRI)
            return (IRI) s;
        }
      }
    }
    return null;
  }
}
