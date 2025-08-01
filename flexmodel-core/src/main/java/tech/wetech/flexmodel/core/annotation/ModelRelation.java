package tech.wetech.flexmodel.core.annotation;

import java.lang.annotation.*;

/**
 * 模型字段标识
 * @author cjbi
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface ModelRelation {
}
