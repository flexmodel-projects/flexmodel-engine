package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class GeneratedValue {

  private String name;

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
}
