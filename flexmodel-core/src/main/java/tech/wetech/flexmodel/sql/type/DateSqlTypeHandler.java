package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.mapping.DateTypeHandler;

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
  public LocalDate getNullableResult(ResultSet rs, String columnName, tech.wetech.flexmodel.Field field) throws SQLException {
    try {
      return rs.getObject(columnName, LocalDate.class);
    } catch (NullPointerException e) {
      return null;
    }
  }
}
