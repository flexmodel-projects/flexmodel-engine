package tech.wetech.flexmodel.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionManaged;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务示例
 * 展示如何使用继承的SessionManager
 *
 * @author cjbi
 */
@ApplicationScoped
@SessionManaged
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  @Inject
  private Session session;

  /**
   * 获取所有用户
   */
  public List<Map<String, Object>> getAllUsers() {
    log.info("Getting all users using session: {}", session.getName());
    return session.data().find("users", Query.Builder.create().build());
  }

  /**
   * 根据ID获取用户
   */
  public Map<String, Object> getUserById(String id) {
    log.info("Getting user by id: {} using session: {}", id, session.getName());
    return session.data().findById("users", id);
  }

  /**
   * 创建用户
   */
  public Map<String, Object> createUser(Map<String, Object> userData) {
    log.info("Creating user using session: {}", session.getName());

    session.startTransaction();
    try {
      int affectedRows = session.data().insert("users", userData);
      session.commit();

      Map<String, Object> result = new HashMap<>();
      result.put("message", "User created successfully");
      result.put("affectedRows", affectedRows);
      result.put("user", userData);
      return result;
    } catch (Exception e) {
      session.rollback();
      throw new RuntimeException("Failed to create user", e);
    }
  }

  /**
   * 更新用户
   */
  public Map<String, Object> updateUser(String id, Map<String, Object> userData) {
    log.info("Updating user with id: {} using session: {}", id, session.getName());

    session.startTransaction();
    try {
      int affectedRows = session.data().updateById("users", userData, id);
      session.commit();

      Map<String, Object> result = new HashMap<>();
      result.put("message", "User updated successfully");
      result.put("id", id);
      result.put("affectedRows", affectedRows);
      return result;
    } catch (Exception e) {
      session.rollback();
      throw new RuntimeException("Failed to update user", e);
    }
  }

  /**
   * 删除用户
   */
  public Map<String, Object> deleteUser(String id) {
    log.info("Deleting user with id: {} using session: {}", id, session.getName());

    session.startTransaction();
    try {
      int affectedRows = session.data().deleteById("users", id);
      session.commit();

      Map<String, Object> result = new HashMap<>();
      result.put("message", "User deleted successfully");
      result.put("id", id);
      result.put("affectedRows", affectedRows);
      return result;
    } catch (Exception e) {
      session.rollback();
      throw new RuntimeException("Failed to delete user", e);
    }
  }
}
