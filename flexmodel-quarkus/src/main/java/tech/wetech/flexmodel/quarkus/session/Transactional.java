package tech.wetech.flexmodel.quarkus.session;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Transactional注解
 * 用于标记需要事务管理的方法或类
 * 
 * 该注解会在方法执行前自动开启事务，在方法正常返回时提交事务，
 * 在方法抛出异常时回滚事务
 * 
 * 注意：使用此注解的方法必须同时使用@SessionManaged注解
 *
 * @author cjbi
 */
@InterceptorBinding
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {

  /**
   * 是否为只读事务
   * 默认为false，表示可读写事务
   */
  boolean readOnly() default false;
}

