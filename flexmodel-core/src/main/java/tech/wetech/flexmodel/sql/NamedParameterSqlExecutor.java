package tech.wetech.flexmodel.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.sql.type.SqlResultHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author cjbi
 */
public class NamedParameterSqlExecutor implements SqlExecutor {

  private final Connection connection;

  private final Logger log = LoggerFactory.getLogger(NamedParameterSqlExecutor.class);

  public NamedParameterSqlExecutor(Connection connection) {
    Objects.requireNonNull(connection);
    this.connection = connection;
  }

  @Override
  public <T> T queryForScalar(String sql, Map<String, Object> paramMap, Class<T> type) {
    return metrics(() -> {
      NamedParamStatement stmt = null;
      ResultSet rs = null;
      try {
        stmt = new NamedParamStatement(connection, sql);
        setParameters(stmt, paramMap);
        rs = stmt.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        if (columnCount != 1) {
          throw new SQLException("Incorrect column count: expected 1, actual " + columnCount);
        }
        if (rs.next()) {
          if (type == null) {
            return (T) rs.getObject(1);
          }
          return rs.getObject(1, type);
        }
        return null;
      } catch (SQLException e) {
        throw new SqlExecutionException("Could not execute JDBC Statement: " + sql + ", Reason: " + e.getMessage(), e);
      } finally {
        closeResultSet(rs);
        closeStatement(stmt);
      }
    }, sql, paramMap);
  }

  @Override
  public Connection getConnection() {
    return connection;
  }


  @Override
  public <T> T queryForScalar(String sql, Class<T> type) {
    return queryForScalar(sql, Collections.emptyMap(), type);
  }

  @Override
  public Map<String, Object> queryForMap(String sql) {
    return queryForMap(sql, Collections.emptyMap());
  }

  public Map<String, Object> queryForMap(String sql, Map<String, Object> paramMap) {
    return queryForObject(sql, paramMap, new SqlResultHandler<>(Map.class));
  }

  @Override
  public <T> T queryForObject(String sql, Map<String, Object> paramMap, SqlResultHandler<T> sqlResultHandler) {
    return metrics(() -> {
      NamedParamStatement stmt = null;
      ResultSet rs = null;
      try {
        stmt = new NamedParamStatement(connection, sql);
        setParameters(stmt, paramMap);
        rs = stmt.executeQuery();
        if (rs.next()) {
          return sqlResultHandler.convertResultSetToObject(rs);
        } else {
          return null;
        }
      } catch (SQLException e) {
        throw new SqlExecutionException("Could not execute JDBC Statement: " + sql + ", Reason: " + e.getMessage(), e);
      } finally {
        closeResultSet(rs);
        closeStatement(stmt);
      }
    }, sql, paramMap);
  }

  @Override
  public List<Map<String, Object>> queryForList(String sql) {
    return queryForList(sql, Collections.emptyMap());
  }

  @Override
  @SuppressWarnings("all")
  public List<Map<String, Object>> queryForList(String sql, Map<String, Object> paramMap) {
    return (List) queryForList(sql, paramMap, new SqlResultHandler<>(Map.class));
  }

  @Override
  public <T> List<T> queryForList(String sql, SqlResultHandler<T> sqlResultHandler) {
    return queryForList(sql, Collections.emptyMap(), sqlResultHandler);
  }

  @Override
  public <T> List<T> queryForList(String sql, Map<String, Object> paramMap, SqlResultHandler<T> sqlResultHandler) {
    return metrics(() -> {
      NamedParamStatement stmt = null;
      ResultSet rs = null;
      try {
        stmt = new NamedParamStatement(connection, sql);
        setParameters(stmt, paramMap);
        rs = stmt.executeQuery();
        return sqlResultHandler.convertResultSetToList(rs);
      } catch (SQLException e) {
        throw new SqlExecutionException("Could not execute JDBC Statement: " + sql + ", Reason: " + e.getMessage(), e);
      } finally {
        closeResultSet(rs);
        closeStatement(stmt);
      }
    }, sql, paramMap);
  }

  @Override
  public Stream<Map<String, Object>> queryForStream(String sql) {
    return queryForStream(sql, Collections.emptyMap());
  }

  @Override
  @SuppressWarnings("all")
  public Stream<Map<String, Object>> queryForStream(String sql, Map<String, Object> paramMap) {
    return (Stream) queryForStream(sql, paramMap, new SqlResultHandler<>(Map.class));
  }

  @Override
  public <T> Stream<T> queryForStream(String sql, Map<String, Object> paramMap, SqlResultHandler<T> sqlResultHandler) {
    UncheckedCloseable close = null;
    try {
      close = UncheckedCloseable.wrap(connection);
      NamedParamStatement stmt = new NamedParamStatement(connection, sql);
      setParameters(stmt, paramMap);
      close = close.nest(stmt);
      connection.setAutoCommit(false);
      ResultSet rs = stmt.executeQuery();
      close = close.nest(rs);
      return StreamSupport.stream(new Spliterators.AbstractSpliterator<T>(
        Long.MAX_VALUE, Spliterator.ORDERED) {
        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
          try {
            if (!rs.next()) return false;
            action.accept(sqlResultHandler.convertResultSetToObject(rs));
            return true;
          } catch (SQLException ex) {
            throw new RuntimeException(ex);
          }
        }
      }, false).onClose(close);
    } catch (SQLException sqlEx) {
      if (close != null)
        try {
          close.close();
        } catch (Exception ex) {
          sqlEx.addSuppressed(ex);
        }
      throw new SqlExecutionException("Could not execute JDBC Statement: " + sql + " Reason: " + sqlEx.getMessage(), sqlEx);
    }
  }

  interface UncheckedCloseable extends Runnable, AutoCloseable {
    default void run() {
      try {
        close();
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }

    static UncheckedCloseable wrap(AutoCloseable c) {
      return c::close;
    }

    default UncheckedCloseable nest(AutoCloseable c) {
      return () -> {
        try (UncheckedCloseable c1 = this) {
          c.close();
        }
      };
    }
  }

  @Override
  public int update(String sql) {
    return update(sql, Collections.emptyMap());
  }

  @Override
  public int update(String sql, Map<String, Object> paramMap) {
    return metrics(() -> {
      NamedParamStatement stmt = null;
      try {
        stmt = new NamedParamStatement(connection, sql);
        setParameters(stmt, paramMap);
        return stmt.executeUpdate();
      } catch (SQLException e) {
        throw new SqlExecutionException("Could not execute JDBC Statement: " + sql + ", Reason: " + e.getMessage(), e);
      } finally {
        closeStatement(stmt);
      }
    }, sql, paramMap);

  }

  @Override
  public int updateAndReturnGeneratedKeys(String sql, String[] generatedKeyColumns, Consumer<List<?>> keys) {
    return updateAndReturnGeneratedKeys(sql, Collections.emptyMap(), generatedKeyColumns, keys);
  }

  @Override
  public int updateAndReturnFirstGeneratedKeys(String sql, Map<String, Object> paramMap, Consumer<Long> keyConsumer) {
    return metrics(() -> {
      NamedParamStatement stmt = null;
      ResultSet krs = null;
      try {
        stmt = new NamedParamStatement(connection, sql, true);
        setParameters(stmt, paramMap);
        int rows = stmt.executeUpdate();
        krs = stmt.getGeneratedKeys();
        List<Long> keyList = new ArrayList<>();
        while (krs.next()) {
          keyList.add(krs.getLong(1));
        }
        keyConsumer.accept(keyList.getFirst());
        return rows;
      } catch (SQLException e) {
        throw new SqlExecutionException("Could not execute JDBC Statement: " + sql + ", Reason: " + e.getMessage(), e);
      } finally {
        closeResultSet(krs);
        closeStatement(stmt);
      }
    }, sql, paramMap);
  }

  @Override
  public int updateAndReturnGeneratedKeys(String sql, Map<String, Object> paramMap, String[] generatedKeyColumns, Consumer<List<?>> keyConsumer) {
    return metrics(() -> {
      NamedParamStatement stmt = null;
      ResultSet krs = null;
      try {
        stmt = new NamedParamStatement(connection, sql, generatedKeyColumns);
        setParameters(stmt, paramMap);
        int rows = stmt.executeUpdate();
        krs = stmt.getGeneratedKeys();
        List<Long> keyList = new ArrayList<>();
        while (krs.next()) {
          keyList.add(krs.getLong(1));
        }
        keyConsumer.accept(keyList);
        return rows;
      } catch (SQLException e) {
        throw new SqlExecutionException("Could not execute JDBC Statement: " + sql + ", Reason: " + e.getMessage(), e);
      } finally {
        closeResultSet(krs);
        closeStatement(stmt);
      }
    }, sql, paramMap);
  }

  private void setParameters(NamedParamStatement stmt, Map<String, Object> paramMap) throws SQLException {
    stmt.setParameters(paramMap);
  }

  private void closeStatement(NamedParamStatement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException ex) {
        log.debug("Could not close JDBC Statement", ex);
      } catch (Throwable ex) {
        // We don't trust the JDBC driver: It might throw RuntimeException or Error.
        log.debug("Unexpected exception on closing JDBC Statement", ex);
      }
    }
  }

  private void closeResultSet(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException ex) {
        log.debug("Could not close JDBC ResultSet", ex);
      } catch (Throwable ex) {
        // We don't trust the JDBC driver: It might throw RuntimeException or Error.
        log.debug("Unexpected exception on closing JDBC ResultSet", ex);
      }
    }
  }

  public <T> T metrics(Supplier<T> supplier, String sql, Map<String, Object> namedParams, Object... params) {
    if (!log.isDebugEnabled()) {
      return supplier.get();
    }
    log.debug(" ==> Executing SQL      : {} ", sql);
    log.debug(" ==> SQL Parameters     : {}", namedParams != null ? formatValueLog(namedParams) : params);
    long startTime = System.currentTimeMillis();
    T t = supplier.get();

    if (t instanceof Collection<?> collection) {
      log.debug(" ==> SQL result         :");
      for (Object o : collection) {
        if (o instanceof Map<?, ?> map) {
          log.debug(" ==> {}", formatValueLog(map));
        } else {
          log.debug(" ==> {}", o);
        }
      }
    } else {
      log.debug(" ==> SQL result         : {}", t);
    }
    log.debug(" ==> SQL Execution time : {} ms", System.currentTimeMillis() - startTime);
    return t;
  }

  private String formatValueLog(Map<?, ?> map) {
    StringBuilder sb = new StringBuilder();
    map.forEach((key, value) -> {
      sb.append(key).append("=").append(value);
      if (value != null) {
        sb.append("(").append(value.getClass().getSimpleName()).append(")");
      }
      sb.append(", ");
    });
    if (!map.isEmpty()) {
      sb.deleteCharAt(sb.length() - 2);
    }
    return sb.toString();
  }

}
