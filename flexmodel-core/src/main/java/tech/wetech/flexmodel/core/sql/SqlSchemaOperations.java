package tech.wetech.flexmodel.core.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.core.SchemaObject;
import tech.wetech.flexmodel.core.model.EntityDefinition;
import tech.wetech.flexmodel.core.model.EnumDefinition;
import tech.wetech.flexmodel.core.model.IndexDefinition;
import tech.wetech.flexmodel.core.model.NativeQueryDefinition;
import tech.wetech.flexmodel.core.model.field.*;
import tech.wetech.flexmodel.core.operation.SchemaOperations;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class SqlSchemaOperations extends BaseSqlStatement implements SchemaOperations {

  private final Logger log = LoggerFactory.getLogger(SqlSchemaOperations.class);

  public SqlSchemaOperations(SqlContext sqlContext) {
    super(sqlContext);
  }

  @Override
  public List<SchemaObject> syncModels() {
    return sqlContext.getMappedModels().sync(sqlContext);
  }

  @Override
  public List<SchemaObject> syncModels(Set<String> modelNames) {
    return sqlContext.getMappedModels().sync(sqlContext, modelNames);
  }

  @Override
  public List<SchemaObject> getAllModels() {
    return sqlContext.getMappedModels().lookup(sqlContext.getSchemaName());
  }

  @Override
  public SchemaObject getModel(String modelName) {
    return sqlContext.getModel(modelName);
  }

  @Override
  public void dropModel(String modelName) {
    SchemaObject model = getModel(modelName);
    if (model instanceof EntityDefinition entity) {
      dropTable(toSqlTable(entity));
    }
  }

  @Override
  public EntityDefinition createEntity(EntityDefinition collection) {
    SqlTable sqlTable = toSqlTable(collection);
    createTable(sqlTable);
    return collection;
  }

  @Override
  public NativeQueryDefinition createNativeQueryModel(NativeQueryDefinition model) {
    return model;
  }

  @Override
  public EnumDefinition createEnum(EnumDefinition anEnum) {
    return anEnum;
  }

  private void createTable(SqlTable sqlTable) {
    String[] sqlCreateString = sqlContext.getSqlDialect().getTableExporter().getSqlCreateString(sqlTable);
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
    }
    createUniqueKeys(sqlTable);
    createIndexes(sqlTable);
  }

  private void dropTable(SqlTable sqlTable) {
    String[] sqlDropString = sqlContext.getSqlDialect().getTableExporter().getSqlDropString(sqlTable);
    for (String sql : sqlDropString) {
      sqlContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public TypedField<?, ?> createField(TypedField<?, ?> field) {
    if (!(field instanceof RelationField)) {
      createColumn(toSqlColumn(field));
    }
    return field;
  }

  @Override
  public TypedField<?, ?> modifyField(TypedField<?, ?> field) {
    try {
      if (!(field instanceof RelationField)) {
        modifyColumn(toSqlColumn(field));
      }
    } catch (Exception e) {
      log.error("Modify field occurred exception： {}", e.getMessage(), e);
    }
    return field;
  }

  private void modifyColumn(SqlColumn sqlColumn) {
    String sqlAlterModifyColumnString = sqlContext.getSqlDialect().getSqlAlterTableModifyColumnString(sqlColumn);
    sqlContext.getJdbcOperations().update(sqlAlterModifyColumnString);
    SqlTable sqlTable = new SqlTable();
    sqlTable.setName(sqlColumn.getTableName());
    if (sqlColumn.isUnique() && !sqlColumn.isPrimaryKey()) {
      sqlTable.createUniqueKey(List.of(sqlColumn));
      createUniqueKeys(sqlTable);
    }
  }

  private void createColumn(SqlColumn sqlColumn) {
    String[] sqlCreateString = sqlContext.getSqlDialect().getColumnExporter().getSqlCreateString(sqlColumn);
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
    }
    SqlTable sqlTable = new SqlTable();
    sqlTable.setName(sqlColumn.getTableName());
    if (sqlColumn.isUnique()) {
      sqlTable.createUniqueKey(List.of(sqlColumn));
      createUniqueKeys(sqlTable);
    }
  }

  @Override
  public void dropField(String modelName, String fieldName) {
    dropColumn(toSqlColumn(new TypedField<>(fieldName, "unknown").setModelName(modelName)));
  }

  private void dropColumn(SqlColumn sqlColumn) {
    String[] sqlDropString = sqlContext.getSqlDialect().getColumnExporter().getSqlDropString(sqlColumn);
    for (String sql : sqlDropString) {
      sqlContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public IndexDefinition createIndex(IndexDefinition index) {
    SqlIndex sqlIndex = toSqlIndex(index);
    StandardIndexExporter indexExporter = sqlContext.getSqlDialect().getIndexExporter();
    String[] sqlCreateString = indexExporter.getSqlCreateString(sqlIndex);
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
    }
    return index;
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    StandardIndexExporter indexExporter = sqlContext.getSqlDialect().getIndexExporter();
    SqlIndex sqlIndex = toSqlIndex(new IndexDefinition(modelName, indexName));
    String[] sqlDropString = indexExporter.getSqlDropString(sqlIndex);
    for (String sql : sqlDropString) {
      sqlContext.getJdbcOperations().update(sql);
    }
  }


  @Override
  public void createSequence(String sequenceKey, int initialValue, int incrementSize) {
    String sequenceName = toPhysicalSequenceString(sequenceKey);
    String[] sqlCreateString = sqlContext.getSqlDialect().getSequenceExporter().getSqlCreateString(new SqlSequence(sequenceName, initialValue, incrementSize));
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public void dropSequence(String sequenceKey) {
    String sequenceName = toPhysicalSequenceString(sequenceKey);
    String[] sqlDropString = sqlContext.getSqlDialect().getSequenceExporter().getSqlDropString(new SqlSequence(sequenceName, 0, 1));
    for (String sql : sqlDropString) {
      sqlContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public long getSequenceNextVal(String sequenceName) {
    return sqlContext.getSqlDialect().getSequenceNextVal(sequenceName, sqlContext.getJdbcOperations());
  }

  private void createIndexes(SqlTable sqlTable) {
    Iterator<SqlIndex> itr = sqlTable.getIndexIterator();
    while (itr.hasNext()) {
      SqlIndex index = itr.next();
      String[] sqlCreateString = sqlContext.getSqlDialect().getIndexExporter().getSqlCreateString(index);
      for (String sql : sqlCreateString) {
        sqlContext.getJdbcOperations().update(sql);
      }
    }
  }

  private void createUniqueKeys(SqlTable sqlTable) {
    Iterator<SqlUniqueKey> ukItr = sqlTable.getUniqueKeyIterator();
    while (ukItr.hasNext()) {
      SqlUniqueKey uniqueKey = ukItr.next();
      String[] sqlCreateString = sqlContext.getSqlDialect().getUniqueKeyExporter().getSqlCreateString(uniqueKey);
      for (String sql : sqlCreateString) {
        sqlContext.getJdbcOperations().update(sql);
      }
    }
  }

  private SqlTable toSqlTable(EntityDefinition entity) {
    String physicalTableName = toPhysicalTableString(entity.getName());
    SqlTable sqlTable = new SqlTable();
    sqlTable.setName(physicalTableName);
    sqlTable.setComment(entity.getComment());
    SqlPrimaryKey primaryKey = new SqlPrimaryKey(sqlTable);
    for (TypedField<?, ?> field : entity.getFields()) {
      field.setModelName(entity.getName());
      if (field instanceof RelationField) {
        continue;
      }
      if (sqlTable.getColumn(field.getName()) != null) {
        throw new RuntimeException(String.format("The field name %s already exists", field.getName()));
      }
      SqlColumn sqlColumn = toSqlColumn(field);
      if (field.isIdentity()) {
        primaryKey.addColumn(sqlColumn);
      }
      sqlTable.addColumn(sqlColumn);
    }
    if (!primaryKey.getColumns().isEmpty()) {
      sqlTable.setPrimaryKey(primaryKey);
    }
    for (IndexDefinition index : entity.getIndexes()) {
      index.setModelName(entity.getName());
      sqlTable.addIndex(toSqlIndex(index));
    }
    return sqlTable;
  }

  private SqlColumn toSqlColumn(TypedField<?, ?> field) {
    if (field instanceof RelationField relationField) {
      SqlColumn associationColumn = new SqlColumn();
      associationColumn.setName(relationField.getForeignField());
      associationColumn.setTableName(
        toPhysicalTableString(relationField.getFrom())
      );
      associationColumn.setSqlTypeCode(
        sqlContext.getTypeHandler(((EntityDefinition) getModel(field.getModelName())).findIdField().orElseThrow()
          .getType()).getJdbcTypeCode()
      );
      return associationColumn;
    } else if (field.isIdentity()) {
      SqlColumn idColumn = new SqlColumn();
      idColumn.setTableName(toPhysicalTableString(field.getModelName()));
      idColumn.setName(field.getName());
      idColumn.setPrimaryKey(true);
      idColumn.setUnique(true);
      idColumn.setSqlTypeCode(sqlContext.getTypeHandler(field.getType()).getJdbcTypeCode());
      idColumn.setAutoIncrement(Objects.equals(field.getDefaultValue(), GeneratedValue.AUTO_INCREMENT));
      idColumn.setComment(field.getComment());
      return idColumn;
    } else {
      SqlColumn aSqlColumn = new SqlColumn();
      aSqlColumn.setUnique(field.isUnique());
      aSqlColumn.setName(field.getName());

      aSqlColumn.setSqlTypeCode(sqlContext.getTypeHandler(field.getType()).getJdbcTypeCode());
      // fixme support large Objects
      aSqlColumn.setNullable(field.isNullable());
      aSqlColumn.setComment(field.getComment());
      aSqlColumn.setTableName(toPhysicalTableString(field.getModelName()));

      switch (field) {
        case StringField stringField -> {
          aSqlColumn.setLength(stringField.getLength());
          if (field.getDefaultValue() != null && !(field.getDefaultValue() instanceof GeneratedValue)) {
            aSqlColumn.setDefaultValue(field.getDefaultValue().toString());
          }
        }
        case FloatField decimalField -> {
          aSqlColumn.setPrecision(decimalField.getPrecision());
          aSqlColumn.setScale(decimalField.getScale());
          if (field.getDefaultValue() != null && !(field.getDefaultValue() instanceof GeneratedValue)) {
            aSqlColumn.setDefaultValue(field.getDefaultValue().toString());
          }
        }
        case JSONField jsonField -> {
          if (field.getDefaultValue() != null && !(field.getDefaultValue() instanceof GeneratedValue)) {
            aSqlColumn.setDefaultValue(sqlContext.getJsonObjectConverter().toJsonString(jsonField.getDefaultValue()));
          }
        }
        case BooleanField booleanField -> {
          if (field.getDefaultValue() != null && !(field.getDefaultValue() instanceof GeneratedValue)) {
            aSqlColumn.setDefaultValue(sqlContext.getSqlDialect().toBooleanValueString((Boolean) booleanField.getDefaultValue()));
          }
        }
        default -> {
          if (field.getDefaultValue() != null && !(field.getDefaultValue() instanceof GeneratedValue)) {
            aSqlColumn.setDefaultValue(field.getDefaultValue().toString());
          }
        }
      }
      return aSqlColumn;
    }
  }

  private SqlIndex toSqlIndex(IndexDefinition index) {
    String physicalTableName = toPhysicalTableString(index.getModelName());
    String physicalIndexName = index.getName() != null ? index.getName()
      : "IDX_" + StringHelper.hashedName(index.getModelName() + index.getFields().stream()
      .map(IndexDefinition.Field::fieldName)
      .collect(Collectors.joining())
    );
    index.setName(physicalIndexName);
    SqlIndex sqlIndex = new SqlIndex();
    sqlIndex.setName(physicalIndexName);
    SqlTable sqlTable = new SqlTable();
    sqlTable.setName(physicalTableName);
    sqlIndex.setTable(sqlTable);
    sqlIndex.setUnique(index.isUnique());
    for (IndexDefinition.Field field : index.getFields()) {
      SqlColumn sqlColumn = new SqlColumn();
      sqlColumn.setName(field.fieldName());
      sqlIndex.addColumn(sqlColumn, field.direction() != null ? field.direction().toString() : null);
    }
    return sqlIndex;
  }

  private String toPhysicalSequenceString(String name) {
    return name;
  }

  private String toPhysicalTableString(String name) {
    EntityDefinition model = (EntityDefinition) sqlContext.getModel(name);
    if (model == null) {
      return name;
    }
    return model.getName();
  }
}
