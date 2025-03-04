package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public enum ScalarType {
  ID("ID"),
  STRING("STRING"),
  TEXT("TEXT"),
  DECIMAL("DECIMAL"),
  INT("INT"),
  BIGINT("BIGINT"),
  BOOLEAN("BOOLEAN"),
  DATETIME("DATETIME"),
  DATE("DATE"),
  JSON("JSON"),
  ENUM("ENUM"),
  RELATION("RELATION");
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
