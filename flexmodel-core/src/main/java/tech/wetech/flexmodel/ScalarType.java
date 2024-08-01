package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public enum ScalarType {
  ID("id"),
  RELATION("relation"),
  STRING("string"),
  TEXT("text"),
  DECIMAL("decimal"),
  INT("int"),
  BIGINT("bigint"),
  BOOLEAN("boolean"),
  DATETIME("datetime"),
  DATE("date"),
  JSON("json");
  private final String type;

  ScalarType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public static ScalarType fromType(String type) {
    for (ScalarType value : ScalarType.values()) {
      if (type.equals(value.type)) {
        return value;
      }
    }
    return null;
  }

}
