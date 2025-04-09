package tech.wetech.flexmodel.graphql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author cjbi
 */
public class I18nUtilTest {

  @Test
  void test() {
    I18nUtil i18n = new I18nUtil();
    Assertions.assertNotNull(i18n.getString("gql.query.comment"));
  }

}
