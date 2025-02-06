package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.DataSourceProvider;

import javax.sql.DataSource;

/**
 * @author cjbi
 */
public record JdbcDataSourceProvider(String id, DataSource dataSource) implements DataSourceProvider {
  @Override
  public String getId() {
    return id;
  }
}
