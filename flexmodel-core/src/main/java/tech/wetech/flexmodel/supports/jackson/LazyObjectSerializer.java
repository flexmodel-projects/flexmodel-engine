package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.RelationField;
import tech.wetech.flexmodel.lazy.ProxyInterface;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class LazyObjectSerializer extends StdSerializer<ProxyInterface> {

  public static final LazyObjectSerializer INSTANCE = new LazyObjectSerializer(ProxyInterface.class);


  protected LazyObjectSerializer(Class<ProxyInterface> t) {
    super(t);
  }

  @Override
  public void serialize(ProxyInterface value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    Map<String, Object> data = new HashMap<>(invokeGetter(value));
    provider.defaultSerializeValue(data, gen);
  }

  private Map<String, Object> invokeGetter(ProxyInterface value) {
    Class<?> aClass = value.originClass();
    Entity entity = value.entityInfo();
    Field[] declaredFields = aClass.getDeclaredFields();
    Map<String, Object> data = new HashMap<>();
    for (Field field : declaredFields) {
      if (entity.getField(field.getName()) instanceof RelationField) {
        continue;
      }
      try {
        String upperCamelCase = toUpperCamelCase(field.getName());
        String getter = "get" + upperCamelCase;
        String is = "is" + upperCamelCase;
        for (Method declaredMethod : aClass.getDeclaredMethods()) {
          if (getter.equals(declaredMethod.getName()) || is.equals(declaredMethod.getName())) {
            data.put(field.getName(), declaredMethod.invoke(value));
          }
        }
      } catch (InvocationTargetException | IllegalAccessException ignored) {
        ignored.printStackTrace();
      }

    }
    return data;
  }

  private String toUpperCamelCase(String str) {
    char[] cs = str.toCharArray();
    cs[0] -= 32;
    return String.valueOf(cs);
  }


}
