package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.AbstractSession;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL数据库的Session实现
 * 负责SQL数据库的事务管理和连接管理
 * 
 * @author cjbi
 */
public class SqlSession extends AbstractSession {

  private final Connection connection;

  public SqlSession(SqlContext sqlContext) {
    super(sqlContext, new SqlDataOperations(sqlContext), new SqlSchemaOperations(sqlContext));
    this.connection = sqlContext.getConnection();
  }

  @Override
  public void startTransaction() {
    try {
      this.connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void commit() {
    try {
      if (!connection.getAutoCommit()) {
        connection.commit();
      }
      this.connection.setAutoCommit(true);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void rollback() {
    try {
      connection.rollback();
      this.connection.setAutoCommit(true);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    try {
      if (!connection.getAutoCommit()) {
        connection.commit();
      }
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
