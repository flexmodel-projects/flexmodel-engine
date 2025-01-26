package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.mapping.UnknownTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author cjbi
 */
public class UnknownSqlTypeHandler extends UnknownTypeHandler implements SqlTypeHandler<Object> {

  @Override
  public int getJdbcTypeCode() {
    return Types.VARCHAR;
  }

  @Override
  public Object getNullableResult(ResultSet rs, String columnName, tech.wetech.flexmodel.Field field) throws SQLException {
    return rs.getObject(columnName);
  }
}
