package tech.wetech.flexmodel.core.annotation;

import java.lang.annotation.*;

/**
 * 模型类
 * @author cjbi
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface ModelClass {

  String value();

}
