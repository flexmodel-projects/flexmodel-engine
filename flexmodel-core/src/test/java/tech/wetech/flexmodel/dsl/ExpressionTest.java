package tech.wetech.flexmodel.dsl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * @author cjbi
 */
public class ExpressionTest {

  @Test
  void test() {
    // 定义字段表达式
    FieldExpression<String> username = new FieldExpression<>("username");
    FieldExpression<String> remark = new FieldExpression<>("remark");
    FieldExpression<Boolean> locked = new FieldExpression<>("locked");
    FieldExpression<String> email = new FieldExpression<>("email");
    FieldExpression<Integer> age = new FieldExpression<>("age");
    FieldExpression<String> registrationDate = new FieldExpression<>("registrationDate");
    FieldExpression<String> lastLogin = new FieldExpression<>("lastLogin");
    FieldExpression<String> createdAt = new FieldExpression<>("createdAt");
    FieldExpression<String> role = new FieldExpression<>("role");
    FieldExpression<String> status = new FieldExpression<>("status");

    // 构建条件
    Predicate condition = username.eq("john_doe")
      .and(remark.contains("aa")
        .or(Expressions.field("locked").eq(false))
        .or(email.ne("jane_doe@example.com"))
        .or(age.gt(18)))
      .and(registrationDate.between("2020-01-01", "2023-01-01")
        .and(age.lt(65))
        .and(lastLogin.lte("2023-01-01"))
        .and(createdAt.startsWith("2022")))
      .and(role.nin(Collections.singletonList("banned"))
        .or(status.in(Arrays.asList("active", "pending"))));

    // 转换为 JSON 格式
    Map<String, Object> map = condition.toMap();
    String jsonString = new JacksonObjectConverter().toJsonString(map);
    System.out.println(jsonString);
    Assertions.assertEquals("{\"_and\":[{\"_and\":[{\"_and\":[{\"username\":{\"_eq\":\"john_doe\"}},{\"_or\":[{\"_or\":[{\"_or\":[{\"remark\":{\"_contains\":\"aa\"}},{\"locked\":{\"_eq\":false}}]},{\"email\":{\"_ne\":\"jane_doe@example.com\"}}]},{\"age\":{\"_gt\":18}}]}]},{\"_and\":[{\"_and\":[{\"_and\":[{\"registrationDate\":{\"_between\":[\"2020-01-01\",\"2023-01-01\"]}},{\"age\":{\"_lt\":65}}]},{\"lastLogin\":{\"_lte\":\"2023-01-01\"}}]},{\"createdAt\":{\"_starts_with\":\"2022\"}}]}]},{\"_or\":[{\"role\":{\"_nin\":[\"banned\"]}},{\"status\":{\"_in\":[\"active\",\"pending\"]}}]}]}", jsonString);
  }

  @Test
  void testAlwaysTrue() {
    Predicate condition = Expressions.TRUE;
    condition = condition.and(Expressions.field("remark").contains("aa").and(Expressions.field("locked").eq(false)));
    condition = condition.or(Expressions.field("age").gt(18));
    // 转换为 JSON 格式
    Map<String, Object> map = condition.toMap();
    String jsonString = new JacksonObjectConverter().toJsonString(map);
    System.out.println(jsonString);
  }

}
