package tech.wetech.flexmodel.test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * UserResource测试用例
 * 测试用户资源的CRUD操作
 *
 * @author cjbi
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

  @BeforeEach
  void setUp() {
    // 每个测试前可以执行清理操作
    // 注意：这里假设使用内存数据库，每个测试之间数据会保留
  }

  @Test
  @Order(1)
  void testGetAllUsers_InitiallyEmpty() {
    given()
      .when().get("/users")
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("", hasSize(0));
  }

  @Test
  @Order(2)
  void testCreateUser() {
    Map<String, Object> userData = new HashMap<>();
    userData.put("username", "testuser");
    userData.put("avatar", "https://example.com/avatar.jpg");
    userData.put("password_hash", "hashedpassword123");

    given()
      .contentType(ContentType.JSON)
      .body(userData)
      .when().post("/users")
      .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("message", is("User created successfully"))
      .body("affectedRows", is(1))
      .body("user", notNullValue())
      .body("user.username", is("testuser"));
  }

  @Test
  @Order(3)
  void testGetAllUsers_AfterCreation() {
    given()
      .when().get("/users")
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("", isA(java.util.List.class))
      .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  @Order(4)
  void testGetUserById_Success() {
    // 先创建一个用户用于测试
    Map<String, Object> userData = new HashMap<>();
    userData.put("username", "getuser" + System.currentTimeMillis());
    userData.put("password_hash", "hash123");

    String userId = given()
      .contentType(ContentType.JSON)
      .body(userData)
      .when().post("/users")
      .then()
      .statusCode(201)
      .extract()
      .path("user.id");

    // 根据ID获取用户
    given()
      .when().get("/users/" + userId)
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("id", is(userId))
      .body("username", notNullValue());
  }

  @Test
  @Order(5)
  void testGetUserById_NotFound() {
    given()
      .when().get("/users/nonexistent-id-12345")
      .then()
      .statusCode(404)
      .body(is("User not found"));
  }

  @Test
  @Order(6)
  void testUpdateUser_Success() {
    // 先创建一个用户
    Map<String, Object> userData = new HashMap<>();
    String username = "updateuser" + System.currentTimeMillis();
    userData.put("username", username);
    userData.put("password_hash", "hash123");

    String userId = given()
      .contentType(ContentType.JSON)
      .body(userData)
      .when().post("/users")
      .then()
      .statusCode(201)
      .extract()
      .path("user.id");

    // 更新用户
    Map<String, Object> updateData = new HashMap<>();
    updateData.put("username", username + "_updated");
    updateData.put("avatar", "https://example.com/new-avatar.jpg");

    given()
      .contentType(ContentType.JSON)
      .body(updateData)
      .when().put("/users/" + userId)
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("message", is("User updated successfully"))
      .body("id", is(userId))
      .body("affectedRows", is(1));

    // 验证更新是否成功
    given()
      .when().get("/users/" + userId)
      .then()
      .statusCode(200)
      .body("username", is(username + "_updated"))
      .body("avatar", is("https://example.com/new-avatar.jpg"));
  }

  @Test
  @Order(7)
  void testDeleteUser_Success() {
    // 先创建一个用户
    Map<String, Object> userData = new HashMap<>();
    userData.put("username", "deleteuser" + System.currentTimeMillis());
    userData.put("password_hash", "hash123");

    String userId = given()
      .contentType(ContentType.JSON)
      .body(userData)
      .when().post("/users")
      .then()
      .statusCode(201)
      .extract()
      .path("user.id");

    // 删除用户
    given()
      .when().delete("/users/" + userId)
      .then()
      .statusCode(200)
      .contentType(ContentType.JSON)
      .body("message", is("User deleted successfully"))
      .body("id", is(userId))
      .body("affectedRows", is(1));

    // 验证用户已被删除
    given()
      .when().get("/users/" + userId)
      .then()
      .statusCode(404);
  }

  @Test
  @Order(8)
  void testDeleteUser_NotFound() {
    given()
      .when().delete("/users/nonexistent-id-12345")
      .then()
      .statusCode(200)  // 删除操作即使不存在也可能返回200
      .contentType(ContentType.JSON)
      .body("affectedRows", is(0));
  }

  @Test
  @Order(9)
  void testCreateUser_WithAllFields() {
    Map<String, Object> userData = new HashMap<>();
    userData.put("username", "fulluser" + System.currentTimeMillis());
    userData.put("avatar", "https://example.com/full-avatar.jpg");
    userData.put("password_hash", "fullpasswordhash");

    given()
      .contentType(ContentType.JSON)
      .body(userData)
      .when().post("/users")
      .then()
      .statusCode(201)
      .contentType(ContentType.JSON)
      .body("message", is("User created successfully"))
      .body("affectedRows", is(1))
      .body("user.username", is(userData.get("username")))
      .body("user.avatar", is(userData.get("avatar")))
      .body("user.passwordHash", is(userData.get("passwordHash")));
  }

}

