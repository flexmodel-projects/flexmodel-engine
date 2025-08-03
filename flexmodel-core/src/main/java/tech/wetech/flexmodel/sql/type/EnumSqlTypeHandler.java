package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.model.field.EnumField;
import tech.wetech.flexmodel.model.field.Field;
import tech.wetech.flexmodel.type.EnumTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class EnumSqlTypeHandler extends EnumTypeHandler implements SqlTypeHandler<Object> {
  @Override
  public int getJdbcTypeCode() {
    return Types.VARCHAR;
  }

  @Override
  public Object convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    EnumField enumField = (EnumField) field;
    if (enumField.isMultiple()) {
      if (value instanceof String[] strings) {
        return String.join(",", strings);
      } else if (value instanceof Collection<?> col) {
        return col.stream()
          .map(Objects::toString)
          .collect(Collectors.joining(","));
      }
    }
    return super.convertParameter(field, value);
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName, Field field) throws SQLException {
    EnumField enumField = (EnumField) field;
    String value = rs.getString(columnName);
    if (enumField.isMultiple()) {
      if (value == null) {
        return List.of();
      }
      return List.of(value.split(","));
    }
    return value;
  }
}
