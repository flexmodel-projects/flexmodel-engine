package tech.wetech.flexmodel;

import com.mongodb.client.MongoDatabase;
import tech.wetech.flexmodel.cache.Cache;
import tech.wetech.flexmodel.mongodb.MongoDataSourceProvider;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;
import tech.wetech.flexmodel.sql.SqlConnectionHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cjbi
 */
public class ConnectionLifeCycleManager {

  private final SqlConnectionHolder sqlConnectionHolder = new SqlConnectionHolder();
  private final Map<String, DataSourceProvider> dataSourceProviderMap = new ConcurrentHashMap<>();
  private final Map<String, MongoDatabase> mongoDatabaseMap = new HashMap<>();
  private Cache cache;

  public SqlConnectionHolder getSqlConnectionHolder() {
    return sqlConnectionHolder;
  }

  public Map<String, MongoDatabase> getMongoDatabaseMap() {
    return mongoDatabaseMap;
  }

  public void addDataSourceProvider(String identifier, DataSourceProvider dataSource) {
    if (dataSource instanceof JdbcDataSourceProvider jdbcDataSourceProvider) {
      sqlConnectionHolder.addDataSource(identifier, jdbcDataSourceProvider.dataSource());
    } else if (dataSource instanceof MongoDataSourceProvider mongoDataSourceProvider) {
      mongoDatabaseMap.put(identifier, mongoDataSourceProvider.mongoDatabase());
    }
    dataSourceProviderMap.put(identifier, dataSource);
  }

  public DataSourceProvider getDataSourceProvider(String identifier) {
    return dataSourceProviderMap.get(identifier);
  }

  public void setCache(Cache cache) {
    this.cache = cache;
  }

  public void destroy() {
    cache.invalidateAll();
    sqlConnectionHolder.destroy();
  }

}
