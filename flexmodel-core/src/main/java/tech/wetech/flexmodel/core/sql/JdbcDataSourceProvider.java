package tech.wetech.flexmodel.core.sql;

import tech.wetech.flexmodel.core.DataSourceProvider;

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
