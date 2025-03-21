package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.mapping.StringTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author cjbi
 */
public class StringSqlTypeHandler extends StringTypeHandler implements SqlTypeHandler<String> {

  @Override
  public int getJdbcTypeCode() {
    return Types.VARCHAR;
  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName, tech.wetech.flexmodel.Field field) throws SQLException {
    return rs.getString(columnName);
  }
}
