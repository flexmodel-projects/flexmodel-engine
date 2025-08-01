package tech.wetech.flexmodel.session;

import tech.wetech.flexmodel.operation.DataOperations;
import tech.wetech.flexmodel.operation.SchemaOperations;

import java.io.Closeable;

/**
 * @author cjbi
 */
public interface Session extends SchemaOperations, DataOperations, Closeable {

  /**
   * 开启事务
   */
  void startTransaction();

  /**
   * 提交事务
   */
  void commit();

  /**
   * 回滚事务
   */
  void rollback();

  /**
   * 关闭连接
   */
  void close();

  SessionFactory getFactory();

  String getName();

}
