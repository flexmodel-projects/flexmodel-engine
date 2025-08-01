package tech.wetech.flexmodel.example;

import tech.wetech.flexmodel.annotation.ModelClass;
import tech.wetech.flexmodel.annotation.ModelField;

/**
 * 用户实体类示例
 *
 * @author cjbi
 */
@ModelClass("users")
public class User {

  @ModelField("id")
  private Long id;

  @ModelField("name")
  private String name;

  @ModelField("email")
  private String email;

  @ModelField("age")
  private Integer age;

  @ModelField("status")
  private String status;

  // 构造函数
  public User() {
  }

  public User(String name, String email, Integer age) {
    this.name = name;
    this.email = email;
    this.age = age;
    this.status = "active";
  }

  // Getter和Setter方法
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "User{" +
           "id=" + id +
           ", name='" + name + '\'' +
           ", email='" + email + '\'' +
           ", age=" + age +
           ", status='" + status + '\'' +
           '}';
  }
}
