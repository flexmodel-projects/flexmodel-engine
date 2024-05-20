package tech.wetech.flexmodel;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

/**
 * @author cjbi
 */
@QuarkusTest
public class FlexModelResourceTest {

  @Test
  public void testList() {
    given()
      .when()
      .get("/flexmodel/list")
      .then()
      .statusCode(200);
  }

}
