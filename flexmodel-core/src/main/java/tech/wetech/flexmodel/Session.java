package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public interface Session extends SchemaOperations, DataOperations {

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

}
