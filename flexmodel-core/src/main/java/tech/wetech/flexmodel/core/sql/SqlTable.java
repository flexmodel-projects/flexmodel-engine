package tech.wetech.flexmodel.core.sql;


import tech.wetech.flexmodel.core.sql.dialect.DialectException;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi@outlook.com
 */
public class SqlTable implements Exportable {

  private String name;

  /**
   * contains all columns, including the primary key
   */
  private final Map<String, SqlColumn> columns = new LinkedHashMap<>();

  private final Map<String, SqlIndex> indexes = new LinkedHashMap<>();
  private final Map<String, SqlUniqueKey> uniqueKeys = new LinkedHashMap<>();
  private final Map<String, SqlForeignKey> foreignKeys = new LinkedHashMap<>();
  private SqlPrimaryKey primaryKey;
  private String comment;

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public SqlColumn getColumn(String name) {
    if (name == null) {
      return null;
    }
    return columns.get(name);
  }

  public void addColumn(SqlColumn sqlColumn) {
    this.columns.put(sqlColumn.getName(), sqlColumn);
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Iterator<SqlColumn> getColumnIterator() {
    return columns.values().iterator();
  }

  public SqlPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(SqlPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  public boolean hasPrimaryKey() {
    return getPrimaryKey() != null;
  }

  public Iterator<SqlIndex> getIndexIterator() {
    return indexes.values().iterator();
  }

  public Iterator<SqlForeignKey> getForeignKeyIterator() {
    return foreignKeys.values().iterator();
  }

  public SqlIndex getIndex(String indexName) {
    return indexes.get(indexName);
  }

  public void addIndex(SqlIndex index) {
    SqlIndex current = indexes.get(index.getName());
    if (current != null) {
      throw new DialectException("Index " + index.getName() + " already exists!");
    }
    indexes.put(index.getName(), index);
  }

  public SqlIndex getOrCreateIndex(String indexName) {
    SqlIndex index = indexes.get(indexName);
    if (index == null) {
      index = new SqlIndex();
      index.setName(indexName);
      index.setTable(this);
      indexes.put(indexName, index);
    }
    return index;
  }


  public SqlUniqueKey addUniqueKey(SqlUniqueKey uniqueKey) {
    SqlUniqueKey current = uniqueKeys.get(uniqueKey.getName());
    if (current != null) {
      throw new DialectException("UniqueKey " + uniqueKey.getName() + " already exists!");
    }
    uniqueKeys.put(uniqueKey.getName(), uniqueKey);
    return uniqueKey;
  }

  public Iterator<SqlUniqueKey> getUniqueKeyIterator() {
    return getUniqueKeys().values().iterator();
  }

  Map<String, SqlUniqueKey> getUniqueKeys() {
    return uniqueKeys;
  }

  public SqlUniqueKey createUniqueKey(List<SqlColumn> keyColumns) {
    String keyName = SqlConstraint.generateName("UK_", this, keyColumns);
    SqlUniqueKey uk = getOrCreateUniqueKey(keyName);
    uk.addColumns(keyColumns.iterator());
    return uk;
  }

  public SqlUniqueKey getUniqueKey(String keyName) {
    return uniqueKeys.get(keyName);
  }

  public SqlUniqueKey getOrCreateUniqueKey(String keyName) {
    SqlUniqueKey uk = uniqueKeys.get(keyName);
    if (uk == null) {
      uk = new SqlUniqueKey();
      uk.setName(keyName);
      uk.setTable(this);
      uniqueKeys.put(keyName, uk);
    }
    return uk;
  }

  public SqlForeignKey createForeignKey(
    List<SqlColumn> keyColumns,
    SqlTable referencedTable,
    List<SqlColumn> referencedColumns
  ) {
    String keyName = SqlConstraint.generateName("FK_", this, keyColumns);
    SqlForeignKey fk = foreignKeys.get(keyName);
    if (fk == null) {
      fk = new SqlForeignKey();
      fk.setName(keyName);
      fk.setTable(this);
      fk.setCascadeDeleteEnabled(true);
      fk.addColumns(keyColumns.iterator());
      fk.setReferencedTable(referencedTable);
      if (referencedColumns != null) {
        fk.setReferencedColumns(referencedColumns);
      }
      foreignKeys.put(keyName, fk);
    }
    return fk;
  }

}
