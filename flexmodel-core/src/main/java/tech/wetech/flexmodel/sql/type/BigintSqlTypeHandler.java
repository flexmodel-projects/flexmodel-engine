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
  public Long getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return rs.getLong(columnName);
  }
}
