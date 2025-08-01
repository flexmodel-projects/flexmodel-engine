package tech.wetech.flexmodel.core.sql.type;

import tech.wetech.flexmodel.core.model.field.Field;
import tech.wetech.flexmodel.core.type.DateTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

/**
 * @author cjbi
 */
public class DateSqlTypeHandler extends DateTypeHandler implements SqlTypeHandler<LocalDate> {

  @Override
  public int getJdbcTypeCode() {
    return Types.DATE;
  }

  @Override
  public LocalDate getNullableResult(ResultSet rs, String columnName, Field field) throws SQLException {
    try {
      return rs.getObject(columnName, LocalDate.class);
    } catch (NullPointerException e) {
      return null;
    }
  }
}
