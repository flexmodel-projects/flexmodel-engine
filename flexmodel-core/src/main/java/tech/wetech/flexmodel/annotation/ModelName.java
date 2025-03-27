package tech.wetech.flexmodel.annotation;

import java.lang.annotation.*;

/**
 * 模型名称
 * @author cjbi
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface ModelName {

  String value();

}
