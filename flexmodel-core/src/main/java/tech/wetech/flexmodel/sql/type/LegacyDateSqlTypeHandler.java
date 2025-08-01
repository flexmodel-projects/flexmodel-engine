package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.model.field.Field;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

/**
 * @author cjbi
 */
@SuppressWarnings("all")
public class LegacyDateSqlTypeHandler implements SqlTypeHandler {

  @Override
  public int getJdbcTypeCode() {
    return Types.DATE;
  }

  @Override
  public Date convertParameter(Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDate localDate) {
      return Date.valueOf(localDate);
    }
    return Date.valueOf(value.toString());
  }

  @Override
  public LocalDate getNullableResult(ResultSet rs, String columnName, Field field) throws SQLException {
    try {
      return rs.getObject(columnName, Date.class).toLocalDate();
    } catch (NullPointerException e) {
      return null;
    }
  }
}
