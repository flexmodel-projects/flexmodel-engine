package tech.wetech.flexmodel;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.session.Session;
import tech.wetech.flexmodel.session.SessionManaged;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Session管理功能测试
 *
 * @author cjbi
 */
@QuarkusTest
public class SessionManagementTest {

  @Inject
  private TestService testService;

  @Test
  public void testSessionManagement() {
    // 测试Session自动管理
    String result = testService.testSessionOperation();
    assertNotNull(result);
    assertTrue(result.contains("Session operation completed"));
  }

  @Test
  public void testTransactionManagement() {
    // 测试事务管理
    String result = testService.testTransactionOperation();
    assertNotNull(result);
    assertTrue(result.contains("Transaction completed"));
  }

  /**
   * 测试服务类
   */
  @SessionManaged
  public static class TestService {

    @Inject
    private Session session;

    public String testSessionOperation() {
      // 验证Session已自动创建
      assertNotNull(session);
      assertNotNull(session.getName());

      // 执行一些基本操作
      try {
        // 这里可以添加一些实际的数据操作测试
        return "Session operation completed for session: " + session.getName();
      } catch (Exception e) {
        return "Session operation failed: " + e.getMessage();
      }
    }

    public String testTransactionOperation() {
      // 测试事务管理
      session.startTransaction();
      try {
        // 模拟一些操作
        assertNotNull(session.data());

        session.commit();
        return "Transaction completed successfully";
      } catch (Exception e) {
        session.rollback();
        return "Transaction failed: " + e.getMessage();
      }
    }
  }
}
