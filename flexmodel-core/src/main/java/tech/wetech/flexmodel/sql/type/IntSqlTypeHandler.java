package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.mapping.IntTypeHandler;

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
  public Integer getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return rs.getInt(columnName);
  }
}
