package tech.wetech.flexmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.sql.SqlContext;

import java.util.Map;

/**
 * @author cjbi
 */
public abstract class AbstractSessionContext {

  private final String schemaName;
  protected final Logger log = LoggerFactory.getLogger(SqlContext.class);
  private final MappedModels mappedModels;
  protected PhysicalNamingStrategy physicalNamingStrategy = new DefaultPhysicalNamingStrategy();

  protected AbstractSessionContext(String schemaName, MappedModels mappedModels) {
    this.schemaName = schemaName;
    this.mappedModels = mappedModels;
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

  public MappedModels getMappedModels() {
    return mappedModels;
  }

  public abstract Map<String, ? extends TypeHandler<?>> getTypeHandlerMap();
}
