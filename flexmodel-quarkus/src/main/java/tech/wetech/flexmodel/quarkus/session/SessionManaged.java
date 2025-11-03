package tech.wetech.flexmodel.quarkus.session;

import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * SessionManaged注解
 * 用于标记需要自动Session管理的方法或类
 * 
 * 该注解会在方法执行前自动创建Session，在方法执行后自动销毁Session
 * 支持在异步操作（如Mutiny）中自动传递Session上下文
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

