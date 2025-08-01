package tech.wetech.flexmodel.core.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.*;

/**
 * 模型字段标识
 *
 * @author cjbi
 */
@JacksonAnnotationsInside
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface ModelField {
  String value();
}
