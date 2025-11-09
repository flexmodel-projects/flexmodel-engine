package tech.wetech.flexmodel.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;

import java.lang.annotation.*;

/**
 * 模型类
 * @author cjbi
 */
@JacksonAnnotationsInside
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface ModelClass {

  String value();

}
