package tech.wetech.flexmodel.jsonlogic.ast;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cjbi
 * @date 2022/9/4
 */
public class JsonLogicParser {

  private static final JsonMapper JSON = new JsonMapper();


  private JsonLogicParser() {
    JSON.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        JSON.configure(SerializationFeature.INDENT_OUTPUT, false);
    //不显示为null的字段
    JSON.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    //序列化枚举是以ordinal()来输出
    SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
//        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
    JSON.registerModule(simpleModule);
    JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public static JsonLogicNode parse(String json) throws JsonLogicParseException {
    try {
      return parse(JSON.readTree(json));
    } catch (IOException e) {
      throw new JsonLogicParseException(e);
    }
  }

  private static JsonLogicNode parse(JsonNode root) throws JsonLogicParseException {

    if (root.isNull()) {
      return JsonLogicNull.NULL;
    }

    if (root.isValueNode()) {
      if (root.isTextual()) {
        return new JsonLogicString(root.asText());
      }

      if (root.isNumber()) {
        return new JsonLogicNumber(root.asDouble());
      }

      if (root.isBoolean() && root.asBoolean()) {
        return JsonLogicBoolean.TRUE;
      } else {
        return JsonLogicBoolean.FALSE;
      }

    }

    if (root.isArray()) {
      List<JsonLogicNode> elements = new ArrayList<>(root.size());
      for (JsonNode element : root) {
        elements.add(parse(element));
      }
      return new JsonLogicArray(elements);
    }

    String key = root.fieldNames().next();
    JsonLogicNode argumentNode = parse(root.get(key));
    JsonLogicArray arguments;
    if (argumentNode instanceof JsonLogicArray) {
      arguments = (JsonLogicArray) argumentNode;
    } else {
      arguments = new JsonLogicArray(Collections.singletonList(argumentNode));
    }
    // Special case for variable handling
    if ("var".equals(key)) {
      JsonLogicNode defaultValue = arguments.size() > 1 ? arguments.get(1) : JsonLogicNull.NULL;
      return new JsonLogicVariable(arguments.size() < 1 ? JsonLogicNull.NULL : arguments.get(0), defaultValue);
    }
    return new JsonLogicOperation(key, arguments);
  }


}
