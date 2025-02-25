package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import tech.wetech.flexmodel.lazy.ProxyInterface;

import java.io.IOException;
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
    Map<String, Object> data = new HashMap<>();
    provider.defaultSerializeValue(data, gen);
  }

  private Map<String,Object> invokeGetter(ProxyInterface value){
    return null;
  }


}
