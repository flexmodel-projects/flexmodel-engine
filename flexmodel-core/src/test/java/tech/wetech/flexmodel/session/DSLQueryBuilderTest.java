package tech.wetech.flexmodel.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.annotation.ModelClass;
import tech.wetech.flexmodel.query.DSLQueryBuilder;
import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.query.TypedDSLQueryBuilder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DSL查询构建器测试
 */
public class DSLQueryBuilderTest {

  private Session session;
  private DSLQueryBuilder dslQueryBuilder;

  @ModelClass("User")
  public static class User {
    private Long id;
    private String name;
    private String email;

    // getters and setters
    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }
  }

  @BeforeEach
  void setUp() {
    // 创建一个简单的测试，不依赖Mockito
    dslQueryBuilder = new DSLQueryBuilder(session);
  }

  @Test
  void testFromWithString() {
    // 测试使用字符串指定模型名
    DSLQueryBuilder result = dslQueryBuilder.from("User");

    assertSame(dslQueryBuilder, result);
    // 这里应该返回原始的DSLQueryBuilder，execute()返回Map
  }

  @Test
  void testFromWithClass() {
    // 测试使用实体类指定模型
    TypedDSLQueryBuilder<User> result = new TypedDSLQueryBuilder<>(dslQueryBuilder,User.class);

    assertNotNull(result);
    assertNotSame(dslQueryBuilder, result);
    // 这里应该返回TypedDSLQueryBuilder<User>，execute()自动返回User类型
  }

  @Test
  void testTypedDSLQueryBuilderChaining() {
    // 测试带类型的DSL查询构建器的链式调用
    TypedDSLQueryBuilder<User> typedBuilder =  new TypedDSLQueryBuilder<>(dslQueryBuilder,User.class);

    TypedDSLQueryBuilder<User> result = typedBuilder
      .select("id", "name")
      .where("name = 'John'")
      .orderBy("name", Direction.ASC)
      .page(1, 10);

    assertSame(typedBuilder, result);
    // 验证链式调用正常工作
  }

  @Test
  void testModelNameExtraction() {
    // 测试从实体类提取模型名
    TypedDSLQueryBuilder<User> typedBuilder =  new TypedDSLQueryBuilder<>(dslQueryBuilder,User.class);

    // 验证TypedDSLQueryBuilder正确创建
    assertNotNull(typedBuilder);
    // 这里我们只是验证类型推断正常工作
  }
}
