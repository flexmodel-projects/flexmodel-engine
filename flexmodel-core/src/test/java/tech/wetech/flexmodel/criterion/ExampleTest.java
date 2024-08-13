package tech.wetech.flexmodel.criterion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author cjbi
 */
class ExampleTest {

  @Test
  void test() {
    Example example = new Example();
    example.createCriteria()
      .equalTo("username", "john_doe")
      .or()
      .equalTo("","")
      .equalTo("","")
      .notEqualTo("email", "jane_doe@example.com")
      .greaterThan("age", 18)
      .greaterThanOrEqualTo("registrationDate", "2020-01-01")
      .lessThan("age", 65)
      .lessThanOrEqualTo("lastLogin", "2023-01-01")
      .in("status", List.of("active", "pending"))
      .or()
      .notIn("role", List.of("banned"))
      .between("createdAt", "2022-01-01", "2022-12-31")
      /*.andNotBetween("deletedAt", "2023-01-01", "2023-12-31")*/

      /*.orNotBetween("bannedUntil", "2023-01-01", "2023-12-31")*/;
    Assertions.assertNotNull(example);
    Assertions.assertNotNull(example.toFilterString());
  }


}
