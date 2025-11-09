package tech.wetech.flexmodel.supports.jackson;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tech.wetech.flexmodel.annotation.ModelField;

/**
 * 自定义注解解析器，优先使用 {@link ModelField} 的值作为字段名称。
 *
 * @author cjbi
 */
public class ModelFieldAnnotationIntrospector extends JacksonAnnotationIntrospector {

  @Override
  public PropertyName findNameForSerialization(Annotated annotated) {
    PropertyName name = super.findNameForSerialization(annotated);
    if (name != null && !name.isEmpty()) {
      return name;
    }
    return findModelFieldName(annotated);
  }

  @Override
  public PropertyName findNameForDeserialization(Annotated annotated) {
    PropertyName name = super.findNameForDeserialization(annotated);
    if (name != null && !name.isEmpty()) {
      return name;
    }
    return findModelFieldName(annotated);
  }

  private PropertyName findModelFieldName(Annotated annotated) {
    ModelField modelField = _findAnnotation(annotated, ModelField.class);
    if (modelField != null) {
      String value = modelField.value();
      if (value != null && !value.isEmpty()) {
        return PropertyName.construct(value);
      }
    }
    return null;
  }
}


