package tech.wetech.flexmodel.graphql;

import groovy.util.logging.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.JsonObjectConverter;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.Map;

/**
 * @author cjbi
 */
@Slf4j
public class FlexmodelInputWhereTransformerTest {

  private static final Logger log = LoggerFactory.getLogger(FlexmodelInputWhereTransformerTest.class);

  @Test
  public void testWhere() {
    JsonObjectConverter converter = new JacksonObjectConverter();
    Map<String, Object> map = converter.parseToObject("""
        {"_and":[{"className":{"_eq":"一年级1班"}},{"_or":[{"classCode":{"_eq":"C_001"}}]}],"classCode":{"_eq":"C_001"}}
      """, Map.class);
    FlexmodelInputWhereTransformer transformer = new FlexmodelInputWhereTransformer();
    Map<String, Object> result = transformer.transform(map);
    // {"and":[{">":["id",2]},{"==":["jack",3]},{"<":["age",21]}]}
    Assertions.assertNotNull(result);
    log.info("result: {}", converter.toJsonString(result));
  }

}
