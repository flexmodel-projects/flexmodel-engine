package tech.wetech.flexmodel.entity;

import tech.wetech.flexmodel.annotation.ModelClass;
import tech.wetech.flexmodel.annotation.ModelField;

/**
 * @author cjbi
 */
@ModelClass("testSnakeToCamels_person")
public class PersonEntity {

  @ModelField("person_id")
  private String personId;
  @ModelField("person_name")
  private String personName;
  @ModelField("person_age")
  private Long personAge;

  public String getPersonId() {
    return personId;
  }

  public void setPersonId(String personId) {
    this.personId = personId;
  }

  public String getPersonName() {
    return personName;
  }

  public void setPersonName(String personName) {
    this.personName = personName;
  }

  public Long getPersonAge() {
    return personAge;
  }

  public void setPersonAge(Long personAge) {
    this.personAge = personAge;
  }
}
