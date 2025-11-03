package tech.wetech.flexmodel.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.entity.FsUser;
import tech.wetech.flexmodel.quarkus.session.SessionManaged;
import tech.wetech.flexmodel.quarkus.session.Transactional;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.session.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务示例
 * 展示如何使用新的@SessionManaged和@Transactional注解
 *
 * @author cjbi
 */
@ApplicationScoped
@SessionManaged
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

  @Inject
  Session session;

  /**
   * 获取所有用户
   */
  public Uni<List<Map<String, Object>>> getAllUsers() {
    log.info("Getting all users using session: {}", session.getName());
    return Uni.createFrom().item(session.data().find("fs_user", Query.Builder.create().build()));
  }

  /**
   * 根据ID获取用户
   */
  public Map<String, Object> getUserById(String id) {
    log.info("Getting user by id: {} using session: {}", id, session.getName());
    return session.data().findById("fs_user", id);
  }

  /**
   * 创建用户（使用@Transactional自动管理事务）
   */
  @Transactional
  public Map<String, Object> createUser(FsUser userData) {
    log.info("Creating user using session: {}", session.getName());

    int affectedRows = session.dsl().insertInto(FsUser.class).values(userData).execute();

    Map<String, Object> result = new HashMap<>();
    result.put("message", "User created successfully");
    result.put("affectedRows", affectedRows);
    result.put("user", userData);
    return result;
  }

  /**
   * 更新用户（使用@Transactional自动管理事务）
   */
  @Transactional
  public Map<String, Object> updateUser(String id, FsUser userData) {
    log.info("Updating user with id: {} using session: {}", id, session.getName());

    int affectedRows = session.dsl().update(FsUser.class).values(userData).execute();

    Map<String, Object> result = new HashMap<>();
    result.put("message", "User updated successfully");
    result.put("id", id);
    result.put("affectedRows", affectedRows);
    return result;
  }

  /**
   * 删除用户（使用@Transactional自动管理事务）
   */
  @Transactional
  public Map<String, Object> deleteUser(String id) {
    log.info("Deleting user with id: {} using session: {}", id, session.getName());

    int affectedRows = session.data().deleteById("fs_user", id);

    Map<String, Object> result = new HashMap<>();
    result.put("message", "User deleted successfully");
    result.put("id", id);
    result.put("affectedRows", affectedRows);
    return result;
  }
}
