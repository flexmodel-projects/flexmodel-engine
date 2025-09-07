package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.model.field.Field;
import tech.wetech.flexmodel.type.LongTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author cjbi
 */
public class LongSqlTypeHandler extends LongTypeHandler implements SqlTypeHandler<Long> {

  @Override
  public int getJdbcTypeCode() {
    return Types.BIGINT;
  }

  @Override
  public Long getNullableResult(ResultSet rs, String columnName, Field field) throws SQLException {
    Object value = rs.getObject(columnName);
    if (value == null) {
      return null;
    }
    if (value instanceof Number num) {
      return num.longValue();
    }
    return Long.valueOf(value.toString());
  }
}
