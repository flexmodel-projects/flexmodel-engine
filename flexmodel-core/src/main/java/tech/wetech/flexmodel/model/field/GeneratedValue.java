package tech.wetech.flexmodel.model.field;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author cjbi
 */
public class GeneratedValue implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  private String name;

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof GeneratedValue val) {
      return this.name.equals(val.name);
    }
    return super.equals(obj);
  }

  public static final GeneratedValue AUTO_INCREMENT = new GeneratedValue("autoIncrement");
  public static final GeneratedValue UUID = new GeneratedValue("uuid");
  public static final GeneratedValue ULID = new GeneratedValue("ulid");
  public static final GeneratedValue NOW = new GeneratedValue("now");

  public GeneratedValue(String name) {
    this.name = name;
  }

  public GeneratedValue() {
  }

  public GeneratedValue setName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "GeneratedValue{" +
           "name='" + name + '\'' +
           '}';
  }
}
