package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.JsonObjectConverter;
import tech.wetech.flexmodel.mapping.JsonTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author cjbi
 */
public class JsonSqlTypeHandler extends JsonTypeHandler implements SqlTypeHandler<Object> {

  public JsonSqlTypeHandler(JsonObjectConverter jsonObjectConverter) {
    super(jsonObjectConverter);
  }

  @Override
  public int getJdbcTypeCode() {
    return Types.JAVA_OBJECT;
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String jsonString = rs.getString(columnName);
    try {
      if (jsonString == null) {
        return null;
      }
      return jsonObjectConverter.parseToObject(jsonString, Object.class);
    } catch (Exception e) {
      return jsonString;
    }
  }

  @Override
  public Object convertParameter(Object value) {
    return jsonObjectConverter.toJsonString(value);
  }

}
