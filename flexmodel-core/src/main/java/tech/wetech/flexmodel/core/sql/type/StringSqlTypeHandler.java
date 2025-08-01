package tech.wetech.flexmodel.core.sql.type;

import tech.wetech.flexmodel.core.model.field.Field;
import tech.wetech.flexmodel.core.type.StringTypeHandler;

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
  public String getNullableResult(ResultSet rs, String columnName, Field field) throws SQLException {
    return rs.getString(columnName);
  }
}
