package tech.wetech.flexmodel.core.sql.type;

import tech.wetech.flexmodel.core.model.field.Field;
import tech.wetech.flexmodel.core.type.BigintTypeHandler;

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
