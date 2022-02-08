package org.dashjoin.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.dashjoin.expression.ExpressionPreviewService;
import org.dashjoin.expression.ExpressionService;
import org.dashjoin.function.FunctionService;
import org.dashjoin.service.tenant.DefaultTenantManager;
import org.dashjoin.service.tenant.TenantManager;
import com.google.common.collect.Sets;
import lombok.extern.java.Log;

/**
 * central service registry. Provides in process getters as well as JAX-RS singletons for services
 * and exception handlers
 */
@ApplicationScoped
// @ApplicationPath("/")
@Log
public class Services { // extends Application {

  public static final String REST_PREFIX = "rest/";

  @Inject
  Services(TenantManager tenantManager) {
    this.tenantManager = tenantManager;

    // Only log env and settings on production startup
    if (log.isLoggable(Level.CONFIG) && System.getenv("GAE_VERSION") != null)
      logConfigSettings();
  }

  public void logConfigSettings() {
    log.info("--- Dashjoin Services Startup Sequence ---");
    log.info(">> System properties:");
    new TreeMap<>(System.getProperties()).forEach((k, v) -> {
      log.info(k + "=" + v);
    });

    log.info(">> Environment:");
    new TreeMap<>(System.getenv()).forEach((k, v) -> {
      log.info(k + "=" + v);
    });
  }

  /**
   * Tenant cache. TODO: should live with TenantManager
   */
  static Map<String, Config> tenantConfigs = new HashMap<>();

  // @Inject
  public TenantManager tenantManager;

  public TenantManager getTenantManager() {
    return tenantManager;
  }

  /**
   * Initializes the config DB. It is setup as union(classpath, file system) with the file system
   * being read/write and having preference over the other DBs. Other providers may be added later
   * on (e.g. when a DB is defined)
   */
  synchronized public Config getConfig() {

    // if (!multiTenancy) {
    // if (config == null)
    // config = pojoDatabase();
    // return config;
    // }

    String id = tenantManager.getTenantId();
    Config c = tenantConfigs.get(id);
    if (c == null) {
      c = pojoDatabase();
      tenantConfigs.put(id, c);

      // make sure all DBs are initialized
      log.info("Starting metadata collection for tenant=" + id);
      c.metadataCollection();
      log.info("Done metadata collection for tenant=" + id);
    }
    return c;
  }

  @Inject
  JSONDatabase persistantDB;

  @Inject
  @JSONReadonlyDatabase
  JSONDatabase readonlyDB;

  @Inject
  PojoDatabase pojoDatabase() {
    PojoDatabase config = new PojoDatabase(this);
    config._cache = new ConcurrentHashMap<>();
    config.name = "config";
    config.ID = "dj/config";
    config._user = persistantDB;
    config._dbs = new CopyOnWriteArrayList<JSONDatabase>();
    JSONDatabase cl = readonlyDB;
    config._dbs.add(cl);
    config._dbs.add(new PolymorphismDatabase());
    return config;
  }

  /**
   * get the ID of this DJ instance
   */
  public String getDashjoinID() {
    // TODO: hard coded string
    return "dj";
  }

  /**
   * legacy Jetty non DI mode
   */
  public Services() {}

  /**
   * define REST services, exception mapper and jackson config.
   * 
   * register swagger at http://localhost:8080/rest/openapi.json. The swagger UI is not included by
   * default. IT needs to be downloaded from https://swagger.io/tools/swagger-ui/ and extracted into
   * any location in the webapps folder (due to CORS constraints, the API metadata cannot be
   * accessed from UIs served from different hosts).
   */
  // @Override
  public Set<Object> getSingletons() {
    if (persistantDB == null) {
      try {
        tenantManager = new DefaultTenantManager();
        persistantDB = new JSONFileDatabase();
        readonlyDB = new JSONClassloaderDatabase();
        Data data = new Data();
        data.services = this;
        QueryEditor.Delegate delegate = new QueryEditor.Delegate();
        delegate.services = this;
        Manage manage = new Manage();
        manage.services = this;
        manage.data = data;
        FunctionService function = new FunctionService();
        // function.services = this;
        FieldUtils.writeField(function, "services", this, true);
        // function.data = data;
        FieldUtils.writeField(function, "data", data, true);
        ExpressionService expression = new ExpressionService();
        // expression.services = this;
        FieldUtils.writeField(expression, "services", this, true);
        // expression.data = data;
        FieldUtils.writeField(expression, "data", data, true);
        // expression.function = function;
        FieldUtils.writeField(expression, "function", function, true);
        // MappingService mapping = new MappingService();
        // mapping.services = this;
        // FieldUtils.writeField(mapping, "services", this, true);
        // mapping.expressionService = expression;
        // FieldUtils.writeField(mapping, "expressionService", expression, true);
        data.expression = expression;
        // expression.manage = manage;
        FieldUtils.writeField(expression, "manage", manage, true);
        ExpressionPreviewService ep = new ExpressionPreviewService();
        // ep.expression = expression;
        FieldUtils.writeField(ep, "expression", expression, true);
        return Sets.newHashSet(data, delegate, manage, function, expression, /* mapping, */ ep,
            new ExMapper(), new JacksonConfig(),
            new ACLContainerRequestFilter() /*
                                             * , new BasicAuth() , new OpenApiResource()
                                             */);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else
      return null; // super.getSingletons();
  }
}
