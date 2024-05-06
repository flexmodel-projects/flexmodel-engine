package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.DataSourceProvider;

import javax.sql.DataSource;

/**
 * @author cjbi
 */
public record JdbcDataSourceProvider(DataSource dataSource) implements DataSourceProvider {
}
