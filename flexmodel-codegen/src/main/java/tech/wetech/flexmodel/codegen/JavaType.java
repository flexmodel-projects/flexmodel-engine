package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.core.model.field.ScalarType;

import static tech.wetech.flexmodel.core.model.field.ScalarType.*;

/**
 * @author cjbi
 */
public enum JavaType {

  RELATION(ScalarType.RELATION_TYPE, null, null, null),
  STRING(STRING_TYPE, null, "String", "String"),
  FLOAT(FLOAT_TYPE, null, "Double", "Double"),
  INT(INT_TYPE, null, "Integer", "Integer"),
  LONG(LONG_TYPE, null, "Long", "Long"),
  BOOLEAN(BOOLEAN_TYPE, null, "Boolean", "Boolean"),
  DATETIME(DATETIME_TYPE, "java.time", "LocalDateTime", "java.time.LocalDateTime"),
  DATE(DATE_TYPE, "java.time", "LocalDate", "java.time.LocalDate"),
  TIME(TIME_TYPE, "java.time", "LocalTime", "java.time.LocalTime"),
  JSON(JSON_TYPE, null, "Object", "Object");

  private final String type;
  private final String typePackage;
  private final String shortTypeName;
  private final String fullTypeName;

  JavaType(String type, String typePackage, String shortTypeName, String fullTypeName) {
    this.type = type;
    this.typePackage = typePackage;
    this.shortTypeName = shortTypeName;
    this.fullTypeName = fullTypeName;
  }

  public static JavaType getTypeInfo(String type) {
    for (JavaType value : JavaType.values()) {
      if (value.getType().equals(type)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Unknown type: " + type);
  }

  public String getType() {
    return type;
  }

  public String getTypePackage() {
    return typePackage;
  }

  public String getShortTypeName() {
    return shortTypeName;
  }

  public String getFullTypeName() {
    return fullTypeName;
  }
}
