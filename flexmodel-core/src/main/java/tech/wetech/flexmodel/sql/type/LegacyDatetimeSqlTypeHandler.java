package tech.wetech.flexmodel.sql.type;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * @author cjbi
 */
@SuppressWarnings("all")
public class LegacyDatetimeSqlTypeHandler implements SqlTypeHandler {
  @Override
  public int getJdbcTypeCode() {
    return Types.TIMESTAMP;
  }

  @Override
  public Timestamp convertParameter(tech.wetech.flexmodel.Field field, Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof LocalDateTime localDateTime) {
      return Timestamp.valueOf(localDateTime);
    }
    return Timestamp.valueOf(value.toString());
  }

  @Override
  public LocalDateTime getNullableResult(ResultSet rs, String columnName, tech.wetech.flexmodel.Field field) throws SQLException {
    try {
      return rs.getObject(columnName, Timestamp.class).toLocalDateTime();
    } catch (NullPointerException e) {
      return null;
    }
  }
}
