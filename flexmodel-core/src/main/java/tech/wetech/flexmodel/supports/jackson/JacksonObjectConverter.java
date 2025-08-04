package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.JsonObjectConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;

/**
 * @author cjbi
 */
public class JacksonObjectConverter implements JsonObjectConverter {

  private static final Logger log = LoggerFactory.getLogger(JacksonObjectConverter.class);
  private final JsonMapper jsonMapper;

  public JacksonObjectConverter() {
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
    ServiceLoader.load(Module.class).forEach(m -> {
      log.info("[flexmodel-engine] 加载Module:{}", m.getClass().getName());
      builder.addModule(m);
    });
    this.jsonMapper = builder.build();
  }

  @Override
  public String toJsonString(Object obj) {
    try {
      return jsonMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public <T> T parseToObject(String jsonString, Class<T> cls) {
    try {
      if (jsonString == null) {
        return null;
      }
      return jsonMapper.readValue(jsonString, cls);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  @SuppressWarnings("all")
  public <T> T convertValue(Object fromValue, Class<T> cls) {
    if (fromValue != null && cls.isAssignableFrom(fromValue.getClass())) {
      return (T) fromValue;
    }
    return jsonMapper.convertValue(fromValue, cls);
  }

  @Override
  public <T> List<T> convertValueList(List<?> fromValues, Class<T> cls) {
    List<T> list = new ArrayList<>();
    for (Object fromValue : fromValues) {
      list.add(convertValue(fromValue, cls));
    }
    return list;
  }

}
