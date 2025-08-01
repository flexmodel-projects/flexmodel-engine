package tech.wetech.flexmodel.core.sql.type;

import tech.wetech.flexmodel.core.model.field.Field;
import tech.wetech.flexmodel.core.type.DecimalTypeHandler;

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
  public Double getNullableResult(ResultSet rs, String columnName, Field field) throws SQLException {
    Object number = rs.getObject(columnName);
    if (number == null) {
      return null;
    }
    return ((Number) number).doubleValue();
  }
}
