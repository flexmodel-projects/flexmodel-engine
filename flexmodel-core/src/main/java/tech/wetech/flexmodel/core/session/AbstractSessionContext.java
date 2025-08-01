package tech.wetech.flexmodel.core.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.core.JsonObjectConverter;
import tech.wetech.flexmodel.core.ModelRepository;
import tech.wetech.flexmodel.core.model.ModelDefinition;
import tech.wetech.flexmodel.core.model.SchemaObject;
import tech.wetech.flexmodel.core.naming.DefaultPhysicalNamingStrategy;
import tech.wetech.flexmodel.core.naming.PhysicalNamingStrategy;
import tech.wetech.flexmodel.core.type.TypeHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public abstract class AbstractSessionContext {

  protected final Logger log = LoggerFactory.getLogger(AbstractSessionContext.class);

  protected final String schemaName;
  protected final ModelRepository mappedModels;
  protected final JsonObjectConverter jsonObjectConverter;
  protected PhysicalNamingStrategy physicalNamingStrategy = new DefaultPhysicalNamingStrategy();
  protected boolean failsafe = false;
  protected int nestedQueryMaxDepth = 5;
  protected final SessionFactory factory;
  protected final Map<String, ModelDefinition> aliasModelMap = new HashMap<>();

  protected AbstractSessionContext(String schemaName, ModelRepository mappedModels, JsonObjectConverter jsonObjectConverter, SessionFactory factory) {
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
    return this.getMappedModels().find(getSchemaName(), name);
  }

  public ModelRepository getMappedModels() {
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
