package tech.wetech.flexmodel;

import tech.wetech.flexmodel.annotation.ModelClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author cjbi
 */
@ModelClass("testTestInsertWhenDtoParam_teacher")
public class TeacherDTO {
  private Long id;
  private String name;
  private LocalDate birthday;
  private boolean isLocked;
  private LocalDateTime createDatetime;
  private Map<String, Object> extra;
  private String description;
  private Integer age;

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

  public LocalDate getBirthday() {
    return birthday;
  }

  public void setBirthday(LocalDate birthday) {
    this.birthday = birthday;
  }

  public boolean isLocked() {
    return isLocked;
  }

  public void setLocked(boolean locked) {
    isLocked = locked;
  }

  public LocalDateTime getCreateDatetime() {
    return createDatetime;
  }

  public void setCreateDatetime(LocalDateTime createDatetime) {
    this.createDatetime = createDatetime;
  }

  public Map<String, Object> getExtra() {
    return extra;
  }

  public void setExtra(Map<String, Object> extra) {
    this.extra = extra;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }
}
