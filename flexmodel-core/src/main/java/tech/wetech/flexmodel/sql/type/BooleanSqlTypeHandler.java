package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.mapping.BooleanTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author cjbi
 */
public class BooleanSqlTypeHandler extends BooleanTypeHandler implements SqlTypeHandler<Boolean> {

  @Override
  public int getJdbcTypeCode() {
    return Types.BOOLEAN;
  }

  @Override
  public Boolean getNullableResult(ResultSet rs, String columnName, tech.wetech.flexmodel.Field field) throws SQLException {
    return rs.getObject(columnName, Boolean.class);
  }
}
