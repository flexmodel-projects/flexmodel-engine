package tech.wetech.flexmodel.core.sql;

import tech.wetech.flexmodel.core.sql.dialect.DialectException;

import java.util.function.Function;

/**
 * @author cjbi
 */
public class SqlFunction {
  private final Function<String[], String> fragment;
  private final int argumentsSize;

  public SqlFunction(Function<String[], String> fragment, int argumentsSize) {
    this.fragment = fragment;
    this.argumentsSize = argumentsSize;
  }

  public SqlFunction(Function<String[], String> fragment) {
    this.fragment = fragment;
    this.argumentsSize = -1;
  }

  public String render(String... argumentsPlaceHolder) {
    if (argumentsSize != -1 && argumentsPlaceHolder.length != argumentsSize) {
      throw new DialectException("Please input the correct arguments");
    }
    return fragment.apply(argumentsPlaceHolder);
  }

}
