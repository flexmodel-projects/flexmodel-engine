package tech.wetech.flexmodel.core.sql;

import tech.wetech.flexmodel.core.sql.dialect.SqlDialect;

import java.util.Locale;
import java.util.Optional;

/**
 * @author cjbi@outlook.com
 */
public class SqlColumn implements Exportable {

  public static final int DEFAULT_LENGTH = 255;
  public static final int DEFAULT_PRECISION = 19;
  public static final int DEFAULT_SCALE = 2;

  private int length = DEFAULT_LENGTH;
  private int precision = DEFAULT_PRECISION;
  private int scale = DEFAULT_SCALE;

  private int typeIndex;
  private boolean nullable = true;
  private boolean unique;
  private String defaultValue;
  private String name;
  private String tableName;
  private Integer sqlTypeCode;

  private boolean primaryKey;
  private boolean autoIncrement;
  private String comment;

  public void setLength(int length) {
    this.length = length;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  public void setScale(int scale) {
    this.scale = scale;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setSqlTypeCode(Integer sqlTypeCode) {
    this.sqlTypeCode = sqlTypeCode;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public int getLength() {
    return length;
  }

  public String getName() {
    return name;
  }

  public Integer getSqlTypeCode() {
    return sqlTypeCode;
  }

  public int getPrecision() {
    return precision;
  }

  public int getScale() {
    return scale;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public boolean isNullable() {
    return nullable;
  }

  public boolean isPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(boolean primaryKey) {
    this.primaryKey = primaryKey;
  }

  public boolean isAutoIncrement() {
    return autoIncrement;
  }

  public void setAutoIncrement(boolean autoIncrement) {
    this.autoIncrement = autoIncrement;
  }

  public String getComment() {
    return comment;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public boolean isUnique() {
    return unique;
  }

  public void setUnique(boolean unique) {
    this.unique = unique;
  }

  public int getTypeIndex() {
    return typeIndex;
  }

  public void setTypeIndex(int typeIndex) {
    this.typeIndex = typeIndex;
  }

  @Override
  public boolean equals(Object object) {
    return object instanceof SqlColumn && this.equals((SqlColumn) object);
  }

  public boolean equals(SqlColumn sqlColumn) {
    if (null == sqlColumn) {
      return false;
    }
    if (this == sqlColumn) {
      return true;
    }
    return name.equalsIgnoreCase(sqlColumn.getName());
  }

  public String getQuotedName(SqlDialect d) {
    return Optional.ofNullable(d.quoteIdentifier(name))
      .map(String::intern)
      .orElse(null);
  }

  public String getCanonicalName() {
    return name.toLowerCase(Locale.ROOT);
  }

  @Override
  public String toString() {
    return getClass().getName() + '(' + getName() + ')';
  }

  @Override
  public Object clone() {
    SqlColumn copy = new SqlColumn();
    copy.setPrimaryKey(primaryKey);
    copy.setAutoIncrement(autoIncrement);
    copy.setTableName(tableName);
    copy.setLength(length);
    copy.setPrecision(precision);
    copy.setScale(scale);
    copy.setTypeIndex(typeIndex);
    copy.setName(name);
    copy.setNullable(nullable);
    copy.setPrecision(precision);
    copy.setUnique(unique);
    copy.setSqlTypeCode(sqlTypeCode);
    copy.setComment(comment);
    copy.setDefaultValue(defaultValue);
    return copy;
  }

}
