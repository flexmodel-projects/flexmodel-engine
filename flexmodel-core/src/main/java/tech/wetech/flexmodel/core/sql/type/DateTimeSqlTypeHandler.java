package tech.wetech.flexmodel.core.sql.type;

import tech.wetech.flexmodel.core.model.field.Field;
import tech.wetech.flexmodel.core.type.DateTimeTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class DateTimeSqlTypeHandler extends DateTimeTypeHandler implements SqlTypeHandler<LocalDateTime> {

  @Override
  public int getJdbcTypeCode() {
    return Types.TIMESTAMP;
  }

  @Override
  public LocalDateTime getNullableResult(ResultSet rs, String columnName, Field field) throws SQLException {
    try {
      return rs.getObject(columnName, LocalDateTime.class);
    } catch (NullPointerException e) {
      return null;
    }
  }
}
