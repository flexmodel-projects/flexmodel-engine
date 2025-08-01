package tech.wetech.flexmodel.core.sql.type;

import tech.wetech.flexmodel.core.model.field.Field;
import tech.wetech.flexmodel.core.type.IntTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author cjbi
 */
public class IntSqlTypeHandler extends IntTypeHandler implements SqlTypeHandler<Integer> {

  @Override
  public int getJdbcTypeCode() {
    return Types.INTEGER;
  }

  @Override
  public Integer getNullableResult(ResultSet rs, String columnName, Field field) throws SQLException {
    Object value = rs.getObject(columnName);
    if (value == null) {
      return null;
    }
    if (value instanceof Number num) {
      return num.intValue();
    }
    return Integer.valueOf(value.toString());
  }
}
