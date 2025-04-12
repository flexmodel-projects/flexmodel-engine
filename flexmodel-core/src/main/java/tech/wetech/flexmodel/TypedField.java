package tech.wetech.flexmodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author cjbi
 */
public class TypedField<T, SELF extends TypedField<T, SELF>> implements Field {
  private String name;
  private final String type;
  private String modelName;
  private String comment;
  private boolean unique;
  private boolean nullable = true;
  private Object defaultValue;
  private Map<String, Object> additionalProperties = new HashMap<>();
  private boolean identity;

  public TypedField(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public SELF setModelName(String modelName) {
    this.modelName = modelName;
    return self();
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public String getComment() {
    return comment;
  }

  public SELF setComment(String comment) {
    this.comment = comment;
    return self();
  }

  public boolean isUnique() {
    return unique;
  }

  public SELF setUnique(boolean unique) {
    this.unique = unique;
    return self();
  }

  public Object getDefaultValue() {
    return defaultValue;
  }

  public SELF setDefaultValue(Object defaultValue) {
    if (defaultValue instanceof Map<?, ?> map && map.containsKey("name")) {
      this.defaultValue = new GeneratedValue(map.get("name").toString());
    } else {
      this.defaultValue = defaultValue;
    }
    return self();
  }


  public String getType() {
    return type;
  }

  public String getModelName() {
    return modelName;
  }

  public boolean isNullable() {
    return nullable;
  }

  @SuppressWarnings("unchecked")
  public SELF setNullable(boolean nullable) {
    this.nullable = nullable;
    return self();
  }

  @Override
  public String toString() {
    return getClass().getName() + '(' + getName() + ')';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TypedField<?, ?> that)) return false;

    if (unique != that.unique) return false;
    if (nullable != that.nullable) return false;
    if (!Objects.equals(name, that.name)) return false;
    if (!Objects.equals(type, that.type)) return false;
    if (!Objects.equals(modelName, that.modelName)) return false;
    if (!Objects.equals(comment, that.comment)) return false;
    return Objects.equals(defaultValue, that.defaultValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, modelName, comment, unique, nullable, defaultValue);
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

  public String getConcreteType() {
    return type;
  }

  public boolean isIdentity() {
    return identity;
  }

  public TypedField<T, SELF> asIdentity(){
    this.identity = true;
    return this;
  }

  public TypedField<T, SELF> setIdentity(boolean identity) {
    this.identity = identity;
    return this;
  }

  @SuppressWarnings("unchecked")
  private SELF self() {
    return (SELF) this;
  }

}
