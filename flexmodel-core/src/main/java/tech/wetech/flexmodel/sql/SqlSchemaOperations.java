package tech.wetech.flexmodel.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.graph.JoinGraphNode;

import java.util.Iterator;
import java.util.List;

import static tech.wetech.flexmodel.RelationField.Cardinality.MANY_TO_MANY;
import static tech.wetech.flexmodel.RelationField.Cardinality.ONE_TO_ONE;

/**
 * @author cjbi
 */
public class SqlSchemaOperations implements SchemaOperations {

  private final SqlContext sqlContext;

  private final Logger log = LoggerFactory.getLogger(SqlSchemaOperations.class);

  public SqlSchemaOperations(SqlContext sqlContext) {
    this.sqlContext = sqlContext;
  }

  @Override
  public List<Model> syncModels() {
    return sqlContext.getMappedModels().sync(sqlContext);
  }

  @Override
  public List<Model> getAllModels() {
    return sqlContext.getMappedModels().lookup(sqlContext.getSchemaName());
  }

  @Override
  public Model getModel(String modelName) {
    return sqlContext.getMappedModels().getModel(sqlContext.getSchemaName(), modelName);
  }

  @Override
  public void dropModel(String modelName) {
    Model model = getModel(modelName);
    if (model instanceof Entity entity) {
      dropTable(toSqlTable(entity));
    } else if (model instanceof View view) {
      dropView(toSqlView(view));
    }
  }

  @Override
  public Entity createEntity(Entity entity) {
    SqlTable sqlTable = toSqlTable(entity);
    createTable(sqlTable);
    for (TypedField<?, ?> typedField : entity.getFields()) {
      if (typedField instanceof RelationField relationField) {
        createRelation(relationField);
      }
    }
    return entity;
  }

  @Override
  public View createView(String viewName, String viewOn, Query query) {
    View view = new View(viewName);
    view.setViewOn(viewOn);
    view.setQuery(query);
    String[] sqlCreateString = sqlContext.getSqlDialect().getViewExporter().getSqlCreateString(toSqlView(view));
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
    }
    return view;
  }

  private void createTable(SqlTable sqlTable) {
    String[] sqlCreateString = sqlContext.getSqlDialect().getTableExporter().getSqlCreateString(sqlTable);
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
    }
    createUniqueKeys(sqlTable);
    createIndexes(sqlTable);
    createForeignKey(sqlTable);
  }

  private void dropView(SqlView sqlView) {
    String[] sqlCreateString = sqlContext.getSqlDialect().getViewExporter().getSqlDropString(sqlView);
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
    }
  }

  private void dropTable(SqlTable sqlTable) {
    String[] sqlDropString = sqlContext.getSqlDialect().getTableExporter().getSqlDropString(sqlTable);
    for (String sql : sqlDropString) {
      sqlContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public TypedField<?, ?> createField(TypedField<?, ?> field) {
    if (field instanceof RelationField relationField) {
      createRelation(relationField);
    } else {
      createColumn(toSqlColumn(field));
    }
    return field;
  }

  private void createRelation(RelationField field) {
    if (field.getCardinality() == MANY_TO_MANY) {
      Entity entity = (Entity) getModel(field.getModelName());
      Entity targetEntity = (Entity) getModel(field.getTargetEntity());
      JoinGraphNode joinGraphNode = new JoinGraphNode(entity, targetEntity, field);

      SqlTable joinTable = new SqlTable();
      joinTable.setName(joinGraphNode.getJoinName());
      SqlColumn joinColumn = new SqlColumn();
      joinColumn.setTableName(joinGraphNode.getJoinName());
      joinColumn.setName(joinGraphNode.getJoinFieldName());
      joinColumn.setSqlTypeCode(sqlContext.getTypeHandler(joinGraphNode.getJoinFieldType()).getJdbcTypeCode());
      joinTable.addColumn(joinColumn);
      SqlColumn inverseJoinColumn = new SqlColumn();
      inverseJoinColumn.setTableName(joinGraphNode.getJoinName());
      inverseJoinColumn.setName(joinGraphNode.getInverseJoinFieldName());
      inverseJoinColumn.setSqlTypeCode(sqlContext.getTypeHandler(joinGraphNode.getInverseJoinFieldType()).getJdbcTypeCode());
      joinTable.addColumn(inverseJoinColumn);

      SqlTable sqlTable = toSqlTable(entity);
      SqlTable targetSqlTable = toSqlTable(targetEntity);

      joinTable.createForeignKey(List.of(joinColumn), sqlTable, List.of(sqlTable.getColumn(entity.findIdField().map(IDField::getName).orElseThrow())));
      joinTable.createForeignKey(List.of(inverseJoinColumn), targetSqlTable, List.of(targetSqlTable.getColumn(field.getTargetField())));
      try {
        createTable(joinTable);
      } catch (Exception ignored) {
      }
    } else {
      createForeignKey(field);
    }
  }

  @Override
  public TypedField<?, ?> modifyField(TypedField<?, ?> field) {
    try {
      if (field instanceof RelationField relationField) {
        createRelation(relationField);
      } else {
        modifyColumn(toSqlColumn(field));
      }
    } catch (Exception e) {
      log.error("Modify field occurred exception： {}", e.getMessage(), e);
    }
    return field;
  }

  private void createForeignKey(SqlTable sqlTable) {
    Iterator<SqlForeignKey> fkIte = sqlTable.getForeignKeyIterator();
    while (fkIte.hasNext()) {
      SqlForeignKey sqlForeignKey = fkIte.next();
      String[] sqlCreateString = sqlContext.getSqlDialect().getForeignKeyExporter().getSqlCreateString(sqlForeignKey);
      for (String sql : sqlCreateString) {
        sqlContext.getJdbcOperations().update(sql);
      }
    }
  }

  private void createForeignKey(RelationField relationField) {
    SqlTable sqlTable = toSqlTable((Entity) sqlContext.getMappedModels().getModel(sqlContext.getSchemaName(), relationField.getModelName()));
    SqlTable referenceTable = toSqlTable((Entity) sqlContext.getMappedModels()
      .getModel(sqlContext.getSchemaName(), relationField.getTargetEntity()));
    SqlColumn keyColumn = referenceTable.getColumn(relationField.getTargetField());
    if (keyColumn == null) {
      throw new RuntimeException("Foreign key [" + relationField.getTargetField() + "] not exists in [" + relationField.getTargetEntity() + "]");
    }
    List<SqlColumn> keyColumns = List.of(keyColumn);
    if (relationField.getCardinality() == ONE_TO_ONE) {
      SqlUniqueKey uniqueKey = referenceTable.createUniqueKey(keyColumns);
      String[] sqlCreateString = sqlContext.getSqlDialect().getUniqueKeyExporter().getSqlCreateString(uniqueKey);
      for (String sql : sqlCreateString) {
        sqlContext.getJdbcOperations().update(sql);
      }
    }
    SqlForeignKey foreignKey = referenceTable.createForeignKey(keyColumns, sqlTable, sqlTable.getPrimaryKey().getColumns());
    foreignKey.setCascadeDeleteEnabled(relationField.isCascadeDelete());
    String[] sqlCreateString = sqlContext.getSqlDialect().getForeignKeyExporter().getSqlCreateString(foreignKey);
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
    }
  }

  private void modifyColumn(SqlColumn sqlColumn) {
    String sqlAlterModifyColumnString = sqlContext.getSqlDialect().getSqlAlterTableModifyColumnString(sqlColumn);
    sqlContext.getJdbcOperations().update(sqlAlterModifyColumnString);
    SqlTable sqlTable = new SqlTable();
    sqlTable.setName(sqlColumn.getTableName());
    if (sqlColumn.isUnique()) {
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
  public void dropField(String entityName, String fieldName) {
    dropColumn(toSqlColumn(new TypedField<>(fieldName, "unknown").setModelName(entityName)));
  }

  private void dropColumn(SqlColumn sqlColumn) {
    String[] sqlDropString = sqlContext.getSqlDialect().getColumnExporter().getSqlDropString(sqlColumn);
    for (String sql : sqlDropString) {
      sqlContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public Index createIndex(Index index) {
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
    SqlIndex sqlIndex = toSqlIndex(new Index(modelName, indexName));
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

  private SqlTable toSqlTable(Entity entity) {
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
      if (field instanceof IDField) {
        primaryKey.addColumn(sqlColumn);
      }
      sqlTable.addColumn(sqlColumn);
    }
    if (!primaryKey.getColumns().isEmpty()) {
      sqlTable.setPrimaryKey(primaryKey);
    }
    for (Index index : entity.getIndexes()) {
      index.setModelName(entity.getName());
      sqlTable.addIndex(toSqlIndex(index));
    }
    return sqlTable;
  }

  private SqlView toSqlView(View view) {
    Query query = view.getQuery();
    String physicalViewName = toPhysicalTableString(view.getName());
    SqlView sqlView = new SqlView();
    sqlView.setName(physicalViewName);
    sqlView.setColumnList(
      view.getFields().stream()
        .map(Field::getName)
        .toList()
    );
    sqlView.setQuery(SqlHelper.toQuerySql(sqlContext, view.getViewOn(), query));
    return sqlView;
  }

  private SqlColumn toSqlColumn(TypedField<?, ?> field) {
    if (field instanceof RelationField relationField) {
      SqlColumn associationColumn = new SqlColumn();
      associationColumn.setName(relationField.getTargetField());
      associationColumn.setTableName(
        toPhysicalTableString(relationField.getTargetEntity())
      );
      associationColumn.setSqlTypeCode(
        sqlContext.getTypeHandler(((Entity) getModel(field.getModelName())).findIdField().orElseThrow()
          .getGeneratedValue().getType()).getJdbcTypeCode()
      );
      return associationColumn;
    } else if (field instanceof IDField idField) {
      SqlColumn idColumn = new SqlColumn();
      idColumn.setTableName(toPhysicalTableString(field.getModelName()));
      idColumn.setName(field.getName());
      idColumn.setPrimaryKey(true);
      idColumn.setSqlTypeCode(sqlContext.getTypeHandler(idField.getGeneratedValue().getType()).getJdbcTypeCode());
      idColumn.setAutoIncrement(idField.getGeneratedValue() == IDField.GeneratedValue.AUTO_INCREMENT);
      idColumn.setComment(field.getComment());
      return idColumn;
    } else {
      SqlColumn aSqlColumn = new SqlColumn();
      aSqlColumn.setUnique(field.isUnique());
      aSqlColumn.setName(field.getName());

      aSqlColumn.setSqlTypeCode(sqlContext.getTypeHandler(field.getType()).getJdbcTypeCode());
      aSqlColumn.setNullable(field.isNullable());
      aSqlColumn.setComment(field.getComment());
      aSqlColumn.setTableName(toPhysicalTableString(field.getModelName()));

      switch (field) {
        case StringField stringField -> {
          aSqlColumn.setLength(stringField.getLength());
          if (field.getDefaultValue() != null) {
            aSqlColumn.setDefaultValue(field.getDefaultValue().toString());
          }
        }
        case DecimalField decimalField -> {
          aSqlColumn.setPrecision(decimalField.getPrecision());
          aSqlColumn.setScale(decimalField.getScale());
          if (field.getDefaultValue() != null) {
            aSqlColumn.setDefaultValue(field.getDefaultValue().toString());
          }
        }
        case JsonField jsonField -> {
          if (field.getDefaultValue() != null) {
            aSqlColumn.setDefaultValue(sqlContext.getJsonObjectConverter().toJsonString(jsonField.getDefaultValue()));
          }
        }
        case BooleanField booleanField -> {
          if (field.getDefaultValue() != null) {
            aSqlColumn.setDefaultValue(sqlContext.getSqlDialect().toBooleanValueString(booleanField.getDefaultValue()));
          }
        }
        default -> {
          if (field.getDefaultValue() != null) {
            aSqlColumn.setDefaultValue(field.getDefaultValue().toString());
          }
        }
      }
      return aSqlColumn;
    }
  }

  private SqlIndex toSqlIndex(Index index) {
    String physicalTableName = toPhysicalTableString(index.getModelName());
    String physicalIndexName = index.getName() != null ? index.getName()
      : "IDX_" + StringHelper.hashedName(index.getModelName() + System.currentTimeMillis());
    index.setName(physicalIndexName);
    SqlIndex sqlIndex = new SqlIndex();
    sqlIndex.setName(physicalIndexName);
    SqlTable sqlTable = new SqlTable();
    sqlTable.setName(physicalTableName);
    sqlIndex.setTable(sqlTable);
    sqlIndex.setUnique(index.isUnique());
    for (Index.Field field : index.getFields()) {
      SqlColumn sqlColumn = new SqlColumn();
      sqlColumn.setName(field.fieldName());
      sqlIndex.addColumn(sqlColumn, field.direction() != null ? field.direction().toString() : null);
    }
    return sqlIndex;
  }

  private String toPhysicalSequenceString(String name) {
    return sqlContext.getPhysicalNamingStrategy().toPhysicalSequenceName(name);
  }

  private String toPhysicalTableString(String name) {
    return sqlContext.getPhysicalNamingStrategy().toPhysicalTableName(name);
  }
}
