package tech.wetech.flexmodel.sql.type;

import tech.wetech.flexmodel.TypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author cjbi
 */
public interface SqlTypeHandler<T> extends TypeHandler<T> {

  int getJdbcTypeCode();

  T convertParameter(Object value);

  default T getResult(ResultSet rs, String columnName) throws SQLException {
    try {
      return getNullableResult(rs, columnName);
    } catch (Exception e) {
      throw new ResultMapException("Error attempting to get column '" + columnName + "' from result set.  Cause: " + e, e);
    }
  }

  T getNullableResult(ResultSet rs, String columnName) throws SQLException;

}
