package tech.wetech.flexmodel.core.sql;

import tech.wetech.flexmodel.core.sql.type.SqlResultHandler;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author cjbi
 */
public interface SqlExecutor {

  <T> T queryForScalar(String sql, Map<String, Object> paramMap, Class<T> type);

  Connection getConnection();

  <T> T queryForScalar(String sql, Class<T> type);

  Map<String, Object> queryForMap(String sql);

  Map<String, Object> queryForMap(String sql, Map<String, Object> paramMap);

  <T> T queryForObject(String sql, Map<String, Object> paramMap, SqlResultHandler<T> sqlResultHandler);

  List<Map<String, Object>> queryForList(String sql);

  List<Map<String, Object>> queryForList(String sql, Map<String, Object> paramMap);

  <T> List<T> queryForList(String sql, SqlResultHandler<T> sqlResultHandler);

  <T> List<T> queryForList(String sql, Map<String, Object> paramMap, SqlResultHandler<T> sqlResultHandler);

  Stream<Map<String, Object>> queryForStream(String sql);

  Stream<Map<String, Object>> queryForStream(String sql, Map<String, Object> paramMap);

  <T> Stream<T> queryForStream(String sql, Map<String, Object> paramMap, SqlResultHandler<T> sqlResultHandler);

  int update(String sql);

  int update(String sql, Map<String, Object> paramMap);

  int batchUpdate(String sql, List<Map<String, Object>> params);

  int updateAndReturnGeneratedKeys(String sql, String[] generatedKeyColumns, Consumer<List<?>> keyConsumer);

  int updateAndReturnFirstGeneratedKeys(String sql, Map<String, Object> paramMap, Consumer<Long> keyConsumer);

  int updateAndReturnGeneratedKeys(String sql, Map<String, Object> paramMap, String[] generatedKeyColumns, Consumer<List<?>> keyConsumer);
}
