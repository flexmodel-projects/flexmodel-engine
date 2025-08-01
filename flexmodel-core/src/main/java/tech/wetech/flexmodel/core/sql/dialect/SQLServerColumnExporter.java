package tech.wetech.flexmodel.core.sql.dialect;

import tech.wetech.flexmodel.core.sql.SqlColumn;
import tech.wetech.flexmodel.core.sql.StandardColumnExporter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
public class SQLServerColumnExporter extends StandardColumnExporter {

  public SQLServerColumnExporter(SqlDialect sqlDialect) {
    super(sqlDialect);
  }

  @Override
  public String[] getSqlDropString(SqlColumn sqlColumn) {
    List<String> sqlList = new ArrayList<>();
    String[] sqlDropString = super.getSqlDropString(sqlColumn);
    String produce = String.format("""
      DECLARE @var0 nvarchar(128)
         SELECT @var0 = name
         FROM sys.default_constraints
         WHERE parent_object_id = object_id(N'%s')
         AND col_name(parent_object_id, parent_column_id) = '%s';
         IF @var0 IS NOT NULL
             EXECUTE('ALTER TABLE %s DROP CONSTRAINT [' + @var0 + ']')
      """, sqlColumn.getTableName(), sqlColumn.getName(), sqlDialect.quoteIdentifier(sqlColumn.getTableName()));
    sqlList.add(produce);
    sqlList.addAll(List.of(sqlDropString));
    return sqlList.toArray(String[]::new);
  }

}
