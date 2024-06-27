package tech.wetech.flexmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.event.DomainEventPublisher;
import tech.wetech.flexmodel.mapping.TypeHandler;
import tech.wetech.flexmodel.sql.SqlContext;

import java.util.Map;

/**
 * @author cjbi
 */
public abstract class AbstractSessionContext {

  protected final String schemaName;
  protected final Logger log = LoggerFactory.getLogger(SqlContext.class);
  protected final MappedModels mappedModels;
  protected final JsonObjectConverter jsonObjectConverter;
  protected PhysicalNamingStrategy physicalNamingStrategy = new DefaultPhysicalNamingStrategy();
  protected boolean failSafe = false;

  protected AbstractSessionContext(String schemaName, MappedModels mappedModels, JsonObjectConverter jsonObjectConverter) {
    this.schemaName = schemaName;
    this.mappedModels = mappedModels;
    this.jsonObjectConverter = jsonObjectConverter;
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

  public <T> void publishEvent(final T aDomainEvent) {
    DomainEventPublisher.instance().publish(aDomainEvent);
  }

  public boolean isFailSafe() {
    return failSafe;
  }

  public void setFailSafe(boolean failSafe) {
    this.failSafe = failSafe;
  }

  public JsonObjectConverter getJsonObjectConverter() {
    return jsonObjectConverter;
  }
}
