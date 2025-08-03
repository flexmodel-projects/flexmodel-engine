package tech.wetech.flexmodel.query;

import java.io.Serializable;

/**
 * @author cjbi
 */
public enum Direction implements Serializable {
  ASC,
  DESC;

  public static Direction fromString(String direction) {
    for (Direction value : Direction.values()) {
      if (value.name().equalsIgnoreCase(direction)) {
        return value;
      }
    }
    throw new IllegalArgumentException("Invalid direction: " + direction);
  }

}
