package tech.wetech.flexmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.mapping.TypeHandler;

import java.util.Map;

/**
 * @author cjbi
 */
public abstract class AbstractSessionContext {

  protected final Logger log = LoggerFactory.getLogger(AbstractSessionContext.class);

  protected final String schemaName;
  protected final MappedModels mappedModels;
  protected final JsonObjectConverter jsonObjectConverter;
  protected PhysicalNamingStrategy physicalNamingStrategy = new DefaultPhysicalNamingStrategy();
  protected boolean failFast = true;
  protected int deepQueryMaxDepth = 5;
  protected final SessionFactory factory;

  protected AbstractSessionContext(String schemaName, MappedModels mappedModels, JsonObjectConverter jsonObjectConverter, SessionFactory factory) {
    this.schemaName = schemaName;
    this.mappedModels = mappedModels;
    this.jsonObjectConverter = jsonObjectConverter;
    this.factory = factory;
  }

  public PhysicalNamingStrategy getPhysicalNamingStrategy() {
    return physicalNamingStrategy;
  }

  public void setPhysicalNamingStrategy(PhysicalNamingStrategy physicalNamingStrategy) {
    this.physicalNamingStrategy = physicalNamingStrategy;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public Model getModel(String modelName) {
    return this.getMappedModels().getModel(getSchemaName(), modelName);
  }

  public MappedModels getMappedModels() {
    return mappedModels;
  }

  public abstract Map<String, ? extends TypeHandler<?>> getTypeHandlerMap();

  public boolean isFailFast() {
    return failFast;
  }

  public void setFailFast(boolean failFast) {
    this.failFast = failFast;
  }

  public JsonObjectConverter getJsonObjectConverter() {
    return jsonObjectConverter;
  }

  public int getDeepQueryMaxDepth() {
    return deepQueryMaxDepth;
  }

  public void setDeepQueryMaxDepth(int deepQueryMaxDepth) {
    this.deepQueryMaxDepth = deepQueryMaxDepth;
  }

  public SessionFactory getFactory() {
    return factory;
  }
}
