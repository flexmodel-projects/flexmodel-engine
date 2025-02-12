package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.mapping.DecimalTypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @author cjbi
 */
public class DecimalSqlTypeHandler extends DecimalTypeHandler implements SqlTypeHandler<Double> {

  @Override
  public int getJdbcTypeCode() {
    return Types.NUMERIC;
  }

  @Override
  public Double getNullableResult(ResultSet rs, String columnName, tech.wetech.flexmodel.Field field) throws SQLException {
    return rs.getObject(columnName, Double.class);
  }
}
