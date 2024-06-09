package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public enum BasicFieldType {
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

  BasicFieldType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public static BasicFieldType fromType(String type) {
    for (BasicFieldType value : BasicFieldType.values()) {
      if (type.equals(value.type)) {
        return value;
      }
    }
    return null;
  }

}
