package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.mapping.BigintTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author cjbi
 */
public class BigintSqlTypeHandler extends BigintTypeHandler implements SqlTypeHandler<Long> {

  @Override
  public int getJdbcTypeCode() {
    return Types.BIGINT;
  }

  @Override
  public Long getNullableResult(ResultSet rs, String columnName, tech.wetech.flexmodel.Field field) throws SQLException {
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
