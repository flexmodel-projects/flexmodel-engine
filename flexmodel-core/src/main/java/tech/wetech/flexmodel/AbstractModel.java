package tech.wetech.flexmodel;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public abstract class AbstractModel<SELF extends AbstractModel<SELF>> implements Model {

  protected String name;
  protected Map<String, Object> additionalProperties = new HashMap<>();

  @Override
  public String getName() {
    return name;
  }

  public SELF setName(String name) {
    this.name = name;
    return self();
  }

  public SELF addAdditionalProperty(String key, Object value) {
    this.additionalProperties.put(key, value);
    return self();
  }

  public SELF setAdditionalProperties(Map<String, Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
    return self();
  }

  public SELF mergeAdditionalProperty(Map<String, Object> thatAdditionalProperties) {
    additionalProperties.putAll(thatAdditionalProperties);
    return self();
  }

  public Map<String, Object> getAdditionalProperties() {
    return additionalProperties;
  }

  @SuppressWarnings("unchecked")
  private SELF self() {
    return (SELF) this;
  }

}
