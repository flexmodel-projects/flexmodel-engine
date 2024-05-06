package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

/**
 * sqlClause 标识符
 *
 * @author cjbi
 */
public class SqlIdentifier {

  private final String value;

  public SqlIdentifier(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }
}
