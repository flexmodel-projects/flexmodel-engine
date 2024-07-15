package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.generator.FixedValueGenerator;
import tech.wetech.flexmodel.generator.ValueGenerator;
import tech.wetech.flexmodel.validator.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author cjbi
 */
public class FlexModelCoreModule extends SimpleModule {

  public FlexModelCoreModule() {
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
    // ValueGenerator
    setMixInAnnotation(ValueGenerator.class, ValueGeneratorMixIn.class);
    setMixInAnnotation(FixedValueGenerator.class, FixedValueGeneratorMixIn.class);
    // ConstraintValidator
    setMixInAnnotation(ConstraintValidator.class, ConstraintValidatorMixIn.class);
    setMixInAnnotation(DateMaxValidator.class, DateMaxValidatorMixIn.class);
    setMixInAnnotation(DateMinValidator.class, DateMinValidatorMixIn.class);
    setMixInAnnotation(DateRangeValidator.class, DateRangeValidatorMixIn.class);
    setMixInAnnotation(DatetimeMaxValidator.class, DatetimeMaxValidatorMixIn.class);
    setMixInAnnotation(DatetimeMinValidator.class, DatetimeMinValidatorMixIn.class);
    setMixInAnnotation(DatetimeRangeValidator.class, DatetimeRangeValidatorMixIn.class);
    setMixInAnnotation(NumberMaxValidator.class, NumberMaxValidatorMixIn.class);
    setMixInAnnotation(NumberMinValidator.class, NumberMinValidatorMixIn.class);
    setMixInAnnotation(NumberRangeValidator.class, NumberRangeValidatorMixIn.class);
    setMixInAnnotation(EmailValidator.class, EmailValidatorMixIn.class);
    setMixInAnnotation(NotNullValidator.class, NotNullValidatorMixIn.class);
    setMixInAnnotation(RegexpValidator.class, RegexpValidatorMixIn.class);
    setMixInAnnotation(URLValidator.class, URLValidatorMixIn.class);

    addSerializer(Date.class, new JsonSerializer<>() {
      @Override
      public void serialize(Date date, JsonGenerator g, SerializerProvider provider) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
        g.writeString(sdf.format(date));
      }
    });
  }
}
