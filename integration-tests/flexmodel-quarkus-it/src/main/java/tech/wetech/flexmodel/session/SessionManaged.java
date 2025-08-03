package tech.wetech.flexmodel.session;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SessionManaged注解
 * 用于标记需要自动Session管理的方法或类
 *
 * @author cjbi
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionManaged {

  /**
   * 指定要使用的模式名称
   * 如果不指定，将使用默认模式
   */
  String schema() default "";
}
