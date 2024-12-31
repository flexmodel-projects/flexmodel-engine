package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
    setMixInAnnotation(Model.class, ModelMixIn.class);
    setMixInAnnotation(Entity.class, ModelMixIn.class);
    setMixInAnnotation(View.class, ModelMixIn.class);
    // Field
    setMixInAnnotation(TypedField.class, TypedFieldMixIn.class);
    setMixInAnnotation(IDField.class, TypedFieldMixIn.class);
    setMixInAnnotation(StringField.class, TypedFieldMixIn.class);
    setMixInAnnotation(TextField.class, TypedFieldMixIn.class);
    setMixInAnnotation(DecimalField.class, TypedFieldMixIn.class);
    setMixInAnnotation(IntField.class, TypedFieldMixIn.class);
    setMixInAnnotation(BigintField.class, TypedFieldMixIn.class);
    setMixInAnnotation(BooleanField.class, TypedFieldMixIn.class);
    setMixInAnnotation(DatetimeField.class, TypedFieldMixIn.class);
    setMixInAnnotation(DateField.class, TypedFieldMixIn.class);
    setMixInAnnotation(JsonField.class, TypedFieldMixIn.class);
    setMixInAnnotation(RelationField.class, TypedFieldMixIn.class);
    // Index
    setMixInAnnotation(Index.class, IndexMixIn.class);

    addSerializer(Date.class, new JsonSerializer<>() {
      @Override
      public void serialize(Date date, JsonGenerator g, SerializerProvider provider) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
        g.writeString(sdf.format(date));
      }
    });
  }
}
