package tech.wetech.flexmodel.sql.type;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * @author cjbi
 */
@SuppressWarnings("all")
public class SqlResultHandler<T> {
  private final Class<T> resultType;
  private final boolean isResultMap;
  private final Map<String, SqlTypeHandler<?>> sqlTypeHanlderMap = new HashMap<>();
  private final List<Field> fields;

  public SqlResultHandler(Class<T> resultType) {
    this.resultType = resultType;
    if (resultType.isAssignableFrom(Map.class)) {
      this.isResultMap = true;
      this.fields = List.of();
    } else {
      this.isResultMap = false;
      Field[] fields = resultType.getDeclaredFields();
      for (Field field : fields) {
        field.setAccessible(true);
      }
      this.fields = List.of(fields);
    }
  }

  public void addSqlTypeHandler(String columnName, SqlTypeHandler<?> typeHandler) {
    sqlTypeHanlderMap.put(columnName, typeHandler);
  }

  @SuppressWarnings("all")
  public SqlTypeHandler getSqlTypeHandler(String columnName) {
    return sqlTypeHanlderMap.getOrDefault(columnName, new UnknownSqlTypeHandler());
  }

  public List<T> convertResultSetToList(ResultSet resultSet) throws SQLException {
    List<T> list = new ArrayList<>();
    while (resultSet.next()) {
      list.add(convertResultSetToObject(resultSet));
    }
    return list;
  }

  public T convertResultSetToObject(ResultSet rs) throws SQLException {
    if (isResultMap) {
      return (T) convertResultSetToMap(rs);
    }
    try {
      T dto = resultType.getConstructor().newInstance();
      for (Field field : fields) {
        String name = field.getName();
        try {
          SqlTypeHandler<?> typeHandler = this.getSqlTypeHandler(name);
          field.set(dto, typeHandler.getResult(rs, name));
//          field.set(dto, field.getType().getConstructor(String.class).newInstance(value));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return dto;
    } catch (Exception e) {
      throw new TypeException("Mapping type fail: " + e.getMessage(), e);
    }
  }

  private Map<String, Object> convertResultSetToMap(ResultSet rs) throws SQLException {
    ResultSetMetaData metaData = rs.getMetaData();
    int columnCount = metaData.getColumnCount();
    Map<String, Object> rowMap = new LinkedHashMap<>();
    for (int i = 1; i <= columnCount; i++) {
      String columnName = metaData.getColumnLabel(i);
      SqlTypeHandler<?> typeHandler = this.getSqlTypeHandler(columnName);
      rowMap.put(columnName, typeHandler.getResult(rs, columnName));
    }
    return rowMap;
  }

}
