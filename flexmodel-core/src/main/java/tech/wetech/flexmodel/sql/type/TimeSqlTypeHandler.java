package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.mapping.TimeTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalTime;

/**
 * @author cjbi
 */
public class TimeSqlTypeHandler extends TimeTypeHandler implements SqlTypeHandler<LocalTime> {

  @Override
  public int getJdbcTypeCode() {
    return Types.TIME;
  }

  @Override
  public LocalTime getNullableResult(ResultSet rs, String columnName, tech.wetech.flexmodel.Field field) throws SQLException {
    try {
      return rs.getObject(columnName, LocalTime.class);
    } catch (NullPointerException e) {
      return null;
    }
  }
}
