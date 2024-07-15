package tech.wetech.flexmodel;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public interface JsonObjectConverter {

  String toJsonString(Object obj);

  <T> T parseToObject(String jsonString, Class<T> cls);

  @SuppressWarnings("unchecked")
  default Map<String, Object> parseToMap(String jsonString) {
    return parseToObject(jsonString, Map.class);
  }

  @SuppressWarnings("unchecked")
  default List<Map<String, Object>> parseToMapList(String jsonString) {
    return parseToObject(jsonString, List.class);
  }

  <T> T convertValue(Object fromValue, Class<T> cls);

  <T> T convertValue(Object fromValue, UserTypeReference<T> typeReference);

  public static class UserTypeReference<T> extends TypeReference<T> {

  }
}
