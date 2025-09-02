package tech.wetech.flexmodel.model.field;

/**
 * @author cjbi
 */
public enum ScalarType {
  STRING(ScalarType.STRING_TYPE),
  FLOAT(ScalarType.FLOAT_TYPE),
  INT(ScalarType.INT_TYPE),
  LONG(ScalarType.LONG_TYPE),
  BOOLEAN(ScalarType.BOOLEAN_TYPE),
  DATETIME(ScalarType.DATETIME_TYPE),
  DATE(ScalarType.DATE_TYPE),
  TIME(ScalarType.TIME_TYPE),
  JSON(ScalarType.JSON_TYPE),
  ENUM(ScalarType.ENUM_TYPE),
  RELATION(ScalarType.RELATION_TYPE);

  private final String type;

  public static final String STRING_TYPE = "String";
  public static final String FLOAT_TYPE = "Float";
  public static final String INT_TYPE = "Int";
  public static final String LONG_TYPE = "Long";
  public static final String BOOLEAN_TYPE = "Boolean";
  public static final String DATETIME_TYPE = "DateTime";
  public static final String DATE_TYPE = "Date";
  public static final String TIME_TYPE = "Time";
  public static final String JSON_TYPE = "JSON";
  public static final String ENUM_TYPE = "EnumRef";
  public static final String RELATION_TYPE = "Relation";

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
