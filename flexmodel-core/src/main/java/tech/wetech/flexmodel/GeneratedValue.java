package tech.wetech.flexmodel;

import java.time.LocalDateTime;

/**
 * @author cjbi
 */
public class GeneratedValue<T> {

  private final String name;

  public static final GeneratedValue<?> AUTO_INCREMENT = new GeneratedValue<>("autoIncrement");
  public static final GeneratedValue<String> UUID = new GeneratedValue<>("uuid") {

    @Override
    public String generateValue() {
      return java.util.UUID.randomUUID().toString();
    }
  };
  public static final GeneratedValue<String> ULID = new GeneratedValue<>("ulid") {
    @Override
    public String generateValue() {
      return tech.wetech.flexmodel.generator.ULID.random().toString();
    }
  };
  public static final GeneratedValue<LocalDateTime> NOW = new GeneratedValue<>("now") {
    @Override
    public LocalDateTime generateValue() {
      return LocalDateTime.now();
    }
  };

  public GeneratedValue(String name) {
    this.name = name;
  }

  public T generateValue() {
    return null;
  }

  public String getName() {
    return name;
  }
}
