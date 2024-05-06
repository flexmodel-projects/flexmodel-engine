package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.*;

import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public class SqlSchemaOperations implements SchemaOperations {

  private final SqlContext sqlContext;

  public SqlSchemaOperations(SqlContext sqlContext) {
    this.sqlContext = sqlContext;
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
  public Entity createEntity(String modelName, UnaryOperator<Entity> entityUnaryOperator) {
    Entity entity = new Entity(modelName);
    entityUnaryOperator.apply(entity);
    SqlTable sqlTable = toSqlTable(entity);
    createTable(sqlTable);
    for (TypedField<?, ?> typedField : entity.fields()) {
      if (typedField instanceof AssociationField associationField) {
        createForeignKey(associationField);
      }
    }
    return entity;
  }

  @Override
  public View createView(String viewName, String viewOn, UnaryOperator<Query> viewUnaryOperator) {
    Query query = new Query();
    viewUnaryOperator.apply(query);
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
  public void createField(String modelName, TypedField<?, ?> field) {
    field.setModelName(modelName);
    if (field instanceof AssociationField associationField) {
      createForeignKey(associationField);
    } else {
      createColumn(toSqlColumn(field));
    }
  }

  private void createForeignKey(AssociationField associationField) {
    SqlTable sqlTable = toSqlTable(sqlContext.getMappedModels().getEntity(sqlContext.getSchemaName(), associationField.modelName()));
    SqlTable referenceTable = toSqlTable(sqlContext.getMappedModels()
      .getEntity(sqlContext.getSchemaName(), associationField.targetEntity()));
    SqlColumn keyColumn = referenceTable.getColumn(associationField.targetField());
    if (keyColumn == null) {
      throw new RuntimeException("Foreign key [" + associationField.targetField() + "] not exists");
    }
    List<SqlColumn> keyColumns = List.of(keyColumn);
    SqlForeignKey foreignKey = referenceTable.createForeignKey(keyColumns, sqlTable, sqlTable.getPrimaryKey().getColumns());
    foreignKey.setCascadeDeleteEnabled(associationField.cascadeDelete());
    String[] sqlCreateString = sqlContext.getSqlDialect().getForeignKeyExporter().getSqlCreateString(foreignKey);
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
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
  public void createIndex(Index index) {
    SqlIndex sqlIndex = toSqlIndex(index);
    StandardIndexExporter indexExporter = sqlContext.getSqlDialect().getIndexExporter();
    String[] sqlCreateString = indexExporter.getSqlCreateString(sqlIndex);
    for (String sql : sqlCreateString) {
      sqlContext.getJdbcOperations().update(sql);
    }
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
    String physicalTableName = toPhysicalTableString(entity.name());
    SqlTable sqlTable = new SqlTable();
    sqlTable.setName(physicalTableName);
    sqlTable.setComment(entity.comment());
    SqlPrimaryKey primaryKey = new SqlPrimaryKey(sqlTable);
    for (TypedField<?, ?> field : entity.fields()) {
      if (field instanceof AssociationField) {
        continue;
      }
      if (sqlTable.getColumn(field.name()) != null) {
        throw new RuntimeException(String.format("The field name %s already exists", field.name()));
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
    for (Index index : entity.indexes()) {
      sqlTable.addIndex(toSqlIndex(index));
    }
    return sqlTable;
  }

  private SqlView toSqlView(View view) {
    Query query = view.query();
    String physicalViewName = toPhysicalTableString(view.name());
    SqlView sqlView = new SqlView();
    sqlView.setName(physicalViewName);
    sqlView.setColumnList(
      view.fields().stream()
        .map(Field::name)
        .toList()
    );
    sqlView.setQuery(SqlHelper.toQuerySql(sqlContext, view.viewOn(), query));
    return sqlView;
  }

  private SqlColumn toSqlColumn(TypedField<?, ?> field) {
    if (field instanceof AssociationField associationField) {
      SqlColumn associationColumn = new SqlColumn();
      associationColumn.setName(associationField.targetField());
      associationColumn.setTableName(
        toPhysicalTableString(associationField.targetEntity())
      );
      associationColumn.setSqlTypeCode(
        sqlContext.getTypeHandler(((Entity) getModel(field.modelName())).idField()
          .generatedValue().type()).getJdbcTypeCode()
      );
      return associationColumn;
    } else if (field instanceof IDField idField) {
      SqlColumn idColumn = new SqlColumn();
      idColumn.setTableName(toPhysicalTableString(field.modelName()));
      idColumn.setName(field.name());
      idColumn.setPrimaryKey(true);
      idColumn.setSqlTypeCode(sqlContext.getTypeHandler(idField.generatedValue().type()).getJdbcTypeCode());
      idColumn.setAutoIncrement(idField.generatedValue() == IDField.DefaultGeneratedValue.IDENTITY);
      idColumn.setComment(field.comment());
      return idColumn;
    } else {
      SqlColumn aSqlColumn = new SqlColumn();
      aSqlColumn.setUnique(field.isUnique());
      aSqlColumn.setName(field.name());

      aSqlColumn.setSqlTypeCode(sqlContext.getTypeHandler(field.type()).getJdbcTypeCode());
      aSqlColumn.setNullable(field.isNullable());
      aSqlColumn.setComment(field.comment());
      aSqlColumn.setTableName(toPhysicalTableString(field.modelName()));

      switch (field) {
        case StringField stringField -> {
          aSqlColumn.setLength(stringField.getLength());
          if (field.defaultValue() != null) {
            aSqlColumn.setDefaultValue(field.defaultValue().toString());
          }
        }
        case DecimalField decimalField -> {
          aSqlColumn.setPrecision(decimalField.getPrecision());
          aSqlColumn.setScale(decimalField.getScale());
          if (field.defaultValue() != null) {
            aSqlColumn.setDefaultValue(field.defaultValue().toString());
          }
        }
        case JsonField jsonField -> {
          if (field.defaultValue() != null) {
            aSqlColumn.setDefaultValue(JsonUtils.getInstance().stringify(jsonField.defaultValue()));
          }
        }
        case BooleanField booleanField -> {
          if (field.defaultValue() != null) {
            aSqlColumn.setDefaultValue(sqlContext.getSqlDialect().toBooleanValueString(booleanField.defaultValue()));
          }
        }
        default -> {
          if (field.defaultValue() != null) {
            aSqlColumn.setDefaultValue(field.defaultValue().toString());
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
      sqlIndex.addColumn(sqlColumn, field.direction().toString());
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
