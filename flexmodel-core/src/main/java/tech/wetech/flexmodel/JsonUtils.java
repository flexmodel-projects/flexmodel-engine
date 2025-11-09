package tech.wetech.flexmodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import tech.wetech.flexmodel.supports.jackson.FlexmodelCoreModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

/**
 * @author cjbi
 */
public class JsonUtils {

  private static final JsonMapper JSON;

  private JsonUtils() {

  }

  static {
    JsonMapper.Builder builder = new JsonMapper().rebuild();
    //
//        JSON.configure(SerializationFeature.INDENT_OUTPUT, false);
    //不显示为null的字段
//    builder.serializationInclusion(JsonInclude.Include.NON_NULL);
    builder.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    builder.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    builder.disable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    builder.disable(FAIL_ON_EMPTY_BEANS);
    builder.addModule(new JavaTimeModule());
    builder.addModule(new FlexmodelCoreModule());
    ServiceLoader.load(Module.class).forEach(builder::addModule);
    JSON = builder.build();
  }

  public static String toJsonString(Object obj) {
    try {
      return JSON.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static <T> T parseToObject(String jsonString, Class<T> cls) {
    try {
      if (jsonString == null) {
        return null;
      }
      return JSON.readValue(jsonString, cls);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @SuppressWarnings("all")
  public static <T> T convertValue(Object fromValue, Class<T> cls) {
    if (fromValue != null && cls.isAssignableFrom(fromValue.getClass())) {
      return (T) fromValue;
    }
    return JSON.convertValue(fromValue, cls);
  }

  public static <T> T updateValue(T target, Object source) {
    try {
      return JSON.updateValue(target, source);
    } catch (JsonMappingException e) {
      e.printStackTrace();
    }
    return target;
  }

  public static <T> List<T> convertValueList(List<?> fromValues, Class<T> cls) {
    List<T> list = new ArrayList<>();
    for (Object fromValue : fromValues) {
      list.add(convertValue(fromValue, cls));
    }
    return list;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> parseToMap(String jsonString) {
    return parseToObject(jsonString, Map.class);
  }

  @SuppressWarnings("unchecked")
  public static List<Map<String, Object>> parseToMapList(String jsonString) {
    return parseToObject(jsonString, List.class);
  }

}
