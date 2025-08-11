package tech.wetech.flexmodel.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.JsonObjectConverter;
import tech.wetech.flexmodel.ModelRegistry;
import tech.wetech.flexmodel.model.ModelDefinition;
import tech.wetech.flexmodel.model.SchemaObject;
import tech.wetech.flexmodel.naming.DefaultPhysicalNamingStrategy;
import tech.wetech.flexmodel.naming.PhysicalNamingStrategy;
import tech.wetech.flexmodel.type.TypeHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public abstract class AbstractSessionContext {

  protected final Logger log = LoggerFactory.getLogger(AbstractSessionContext.class);

  protected final String schemaName;
  protected final ModelRegistry mappedModels;
  protected final JsonObjectConverter jsonObjectConverter;
  protected PhysicalNamingStrategy physicalNamingStrategy = new DefaultPhysicalNamingStrategy();
  protected boolean failsafe = false;
  protected int nestedQueryMaxDepth = 5;
  protected final SessionFactory factory;
  protected final Map<String, ModelDefinition> aliasModelMap = new HashMap<>();

  protected AbstractSessionContext(String schemaName, ModelRegistry mappedModels, JsonObjectConverter jsonObjectConverter, SessionFactory factory) {
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

  public void addAliasModelIfPresent(String alias, ModelDefinition model) {
    if (!aliasModelMap.containsKey(alias)) {
      aliasModelMap.put(alias, model);
    }
  }

  public SchemaObject getModel(String name) {
    ModelDefinition model = aliasModelMap.get(name);
    if (model != null) {
      return model;
    }
    return this.getModelRegistry().getRegistered(getSchemaName(), name);
  }

  public ModelRegistry getModelRegistry() {
    return mappedModels;
  }

  public abstract Map<String, ? extends TypeHandler<?>> getTypeHandlerMap();

  public boolean isFailsafe() {
    return failsafe;
  }

  public void setFailsafe(boolean failsafe) {
    this.failsafe = failsafe;
  }

  public JsonObjectConverter getJsonObjectConverter() {
    return jsonObjectConverter;
  }

  public int getNestedQueryMaxDepth() {
    return nestedQueryMaxDepth;
  }

  public void setNestedQueryMaxDepth(int nestedQueryMaxDepth) {
    this.nestedQueryMaxDepth = nestedQueryMaxDepth;
  }

  public SessionFactory getFactory() {
    return factory;
  }
}
