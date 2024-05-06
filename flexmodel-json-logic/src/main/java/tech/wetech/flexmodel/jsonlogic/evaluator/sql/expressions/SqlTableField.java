package tech.wetech.flexmodel.jsonlogic.evaluator.sql.expressions;

/**
 * @author cjbi
 */
public class SqlTableField extends SqlIdentifier {
  private final String table;
  private final String field;

  public SqlTableField(String table, String field, String value) {
    super(value);
    this.table = table;
    this.field = field;
  }

  public String getTable() {
    return table;
  }

  public String getField() {
    return field;
  }
}
