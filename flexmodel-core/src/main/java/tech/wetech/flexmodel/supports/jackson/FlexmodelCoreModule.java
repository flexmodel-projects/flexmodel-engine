package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author cjbi
 */
public class FlexmodelCoreModule extends SimpleModule {

  public FlexmodelCoreModule() {
    // Model
    setMixInAnnotation(SchemaObject.class, ModelMixIn.class);
    setMixInAnnotation(Model.class, ModelMixIn.class);
    setMixInAnnotation(Entity.class, ModelMixIn.class);
    setMixInAnnotation(Enum.class, ModelMixIn.class);
    setMixInAnnotation(NativeQueryModel.class, ModelMixIn.class);
    // Field
    setMixInAnnotation(TypedField.class, TypedFieldMixIn.class);
    setMixInAnnotation(StringField.class, TypedFieldMixIn.class);
    setMixInAnnotation(FloatField.class, TypedFieldMixIn.class);
    setMixInAnnotation(IntField.class, TypedFieldMixIn.class);
    setMixInAnnotation(LongField.class, TypedFieldMixIn.class);
    setMixInAnnotation(BooleanField.class, TypedFieldMixIn.class);
    setMixInAnnotation(DateTimeField.class, TypedFieldMixIn.class);
    setMixInAnnotation(DateField.class, TypedFieldMixIn.class);
    setMixInAnnotation(TimeField.class, TypedFieldMixIn.class);
    setMixInAnnotation(JSONField.class, TypedFieldMixIn.class);
    setMixInAnnotation(RelationField.class, TypedFieldMixIn.class);
    setMixInAnnotation(EnumField.class, TypedFieldMixIn.class);
    // Index
    setMixInAnnotation(Index.class, IndexMixIn.class);

    // Date类型序列化
    addSerializer(Date.class, new JsonSerializer<>() {
      @Override
      public void serialize(Date date, JsonGenerator g, SerializerProvider provider) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
        g.writeString(sdf.format(date));
      }
    });

//    addSerializer(ProxyInterface.class, LazyObjectSerializer.INSTANCE);

  }
}
