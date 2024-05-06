package tech.wetech.flexmodel.jsonlogic.evaluator.mongodb.expressions;

import tech.wetech.flexmodel.jsonlogic.evaluator.JsonLogicExpression;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author cjbi
 */
public abstract class MongoExpression implements JsonLogicExpression {

  public String simpleRenderTemplate(String template, Map<String, Object> attributes) {
    int length = template.length();
    for (int i = 0; i < length; i++) {
      if (template.charAt(i) == '<') {
        if (length > i + 1) {
          int j = i;
          template = simpleRenderTemplate(template, length, ++j, attributes);
          length = template.length();
        }
      }
    }
    return template;
  }

  private String simpleRenderTemplate(String template, int length, int i, Map<?, ?> attributes) {
    StringBuilder valueBuilder = new StringBuilder();
    int endIndex = i - 1;
    label:
    for (; i < length; i++) {
      char c1 = template.charAt(i);
      switch (c1) {
        case ' ':
          continue;
        case '>':
          if (length > i + 1) {
            break label;
          }
        default:
          valueBuilder.append(c1);
      }
    }
    String keyString = valueBuilder.toString();
    Object value = attributes;
    if (attributes.get(keyString) instanceof String) {
      value = attributes.get(keyString);
    } else {
      String[] keys = keyString.split("\\.");
      for (String key : keys) {
        if (value instanceof Map) {
          value = ((Map<?, ?>) value).get(key);
        } else {
          value = null;
        }
      }
    }
    return template.substring(0, endIndex) + value + template.substring(++i);
  }

  public String format(Object value) {
    if (value == null) {
      return "null";
    }
    if (value instanceof Number) {
      return value.toString();
    }
    if (value instanceof LocalDateTime || value instanceof LocalDate) {
      return "ISODate(\"" + value + "\")";
    }
    return "\"" + value + "\"";
  }


}
