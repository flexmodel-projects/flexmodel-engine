package tech.wetech.flexmodel.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.ModelRegistry;
import tech.wetech.flexmodel.model.*;
import tech.wetech.flexmodel.model.field.*;
import tech.wetech.flexmodel.service.BaseService;
import tech.wetech.flexmodel.service.SchemaService;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class SqlSchemaService extends BaseService implements SchemaService {

  private final Logger log = LoggerFactory.getLogger(SqlSchemaService.class);
  private final SqlContext sessionContext;
  private final ModelRegistry modelRegistry;

  public SqlSchemaService(SqlContext sessionContext) {
    super(sessionContext);
    this.sessionContext = sessionContext;
    this.modelRegistry = sessionContext.getModelRegistry();
  }

  @Override
  public List<SchemaObject> loadModels() {
    return sessionContext.getModelRegistry().loadFromDataSource(sessionContext);
  }

  @Override
  public List<SchemaObject> loadModels(Set<String> modelNames) {
    return sessionContext.getModelRegistry().loadFromDataSource(sessionContext, modelNames);
  }

  @Override
  public List<SchemaObject> listModels() {
    return sessionContext.getModelRegistry().listRegistered(sessionContext.getSchemaName());
  }

  @Override
  public SchemaObject getModel(String modelName) {
    return sessionContext.getModelDefinition(modelName);
  }

  @Override
  public void dropModel(String modelName) {
    SchemaObject model = getModel(modelName);
    if (model instanceof EntityDefinition entity) {
      dropTable(toSqlTable(entity));
    }
    modelRegistry.unregisterAll(sessionContext.getSchemaName(), modelName);
  }

  @Override
  public EntityDefinition createEntity(EntityDefinition entity) {
    // 保存到ModelRegistry中
    modelRegistry.register(sessionContext.getSchemaName(), entity);
    SqlTable sqlTable = toSqlTable(entity);
    createTable(sqlTable);
    return entity;
  }

  @Override
  public NativeQueryDefinition createNativeQuery(NativeQueryDefinition nq) {
    modelRegistry.register(sessionContext.getSchemaName(), nq);
    return nq;
  }

  @Override
  public EnumDefinition createEnum(EnumDefinition anEnum) {
    // 保存到ModelRegistry中
    modelRegistry.register(sessionContext.getSchemaName(), anEnum);
    return anEnum;
  }

  private void createTable(SqlTable sqlTable) {
    String[] sqlCreateString = sessionContext.getSqlDialect().getTableExporter().getSqlCreateString(sqlTable);
    for (String sql : sqlCreateString) {
      sessionContext.getJdbcOperations().update(sql);
    }
    createUniqueKeys(sqlTable);
    createIndexes(sqlTable);
  }

  private void dropTable(SqlTable sqlTable) {
    String[] sqlDropString = sessionContext.getSqlDialect().getTableExporter().getSqlDropString(sqlTable);
    for (String sql : sqlDropString) {
      sessionContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public TypedField<?, ?> createField(TypedField<?, ?> field) {
    // 更新实体定义，添加新字段
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(field.getModelName());
    if (entity != null) {
      entity.addField(field);
      modelRegistry.register(sessionContext.getSchemaName(), entity);
    }
    if (!(field instanceof RelationField)) {
      createColumn(toSqlColumn(field));
    }
    return field;
  }

  @Override
  public TypedField<?, ?> modifyField(TypedField<?, ?> field) {
    // 更新实体定义，修改字段
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(field.getModelName());
    if (entity != null) {
      entity.removeField(field.getName());
      entity.addField(field);
      modelRegistry.register(sessionContext.getSchemaName(), entity);
    }
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
    String sqlAlterModifyColumnString = sessionContext.getSqlDialect().getSqlAlterTableModifyColumnString(sqlColumn);
    sessionContext.getJdbcOperations().update(sqlAlterModifyColumnString);
    SqlTable sqlTable = new SqlTable();
    sqlTable.setName(sqlColumn.getTableName());
    if (sqlColumn.isUnique() && !sqlColumn.isPrimaryKey()) {
      sqlTable.createUniqueKey(List.of(sqlColumn));
      createUniqueKeys(sqlTable);
    }
  }

  private void createColumn(SqlColumn sqlColumn) {
    String[] sqlCreateString = sessionContext.getSqlDialect().getColumnExporter().getSqlCreateString(sqlColumn);
    for (String sql : sqlCreateString) {
      sessionContext.getJdbcOperations().update(sql);
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
    try{
      dropColumn(toSqlColumn(new TypedField<>(fieldName, "unknown").setModelName(modelName)));
    }catch (Exception e){
      log.error("Drop field occurred exception： {}", e.getMessage(), e);
    }
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
    entity.removeField(fieldName);
    // 移除相关索引
    for (IndexDefinition index : entity.getIndexes()) {
      if (index.containsField(fieldName)) {
        entity.removeIndex(index.getName());
      }
    }
    sessionContext.getModelRegistry().register(sessionContext.getSchemaName(), entity);
  }

  private void dropColumn(SqlColumn sqlColumn) {
    String[] sqlDropString = sessionContext.getSqlDialect().getColumnExporter().getSqlDropString(sqlColumn);
    for (String sql : sqlDropString) {
      sessionContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public IndexDefinition createIndex(IndexDefinition index) {
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(index.getModelName());
    entity.addIndex(index);
    modelRegistry.register(sessionContext.getSchemaName(), entity);

    SqlIndex sqlIndex = toSqlIndex(index);
    StandardIndexExporter indexExporter = sessionContext.getSqlDialect().getIndexExporter();
    String[] sqlCreateString = indexExporter.getSqlCreateString(sqlIndex);
    for (String sql : sqlCreateString) {
      sessionContext.getJdbcOperations().update(sql);
    }
    return index;
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    EntityDefinition entity = (EntityDefinition) sessionContext.getModelDefinition(modelName);
    entity.removeIndex(indexName);
    modelRegistry.register(sessionContext.getSchemaName(), entity);
    StandardIndexExporter indexExporter = sessionContext.getSqlDialect().getIndexExporter();
    SqlIndex sqlIndex = toSqlIndex(new IndexDefinition(modelName, indexName));
    String[] sqlDropString = indexExporter.getSqlDropString(sqlIndex);
    for (String sql : sqlDropString) {
      sessionContext.getJdbcOperations().update(sql);
    }
  }


  @Override
  public void createSequence(String sequenceKey, int initialValue, int incrementSize) {
    String sequenceName = toPhysicalSequenceString(sequenceKey);
    String[] sqlCreateString = sessionContext.getSqlDialect().getSequenceExporter().getSqlCreateString(new SqlSequence(sequenceName, initialValue, incrementSize));
    for (String sql : sqlCreateString) {
      sessionContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public void dropSequence(String sequenceKey) {
    String sequenceName = toPhysicalSequenceString(sequenceKey);
    String[] sqlDropString = sessionContext.getSqlDialect().getSequenceExporter().getSqlDropString(new SqlSequence(sequenceName, 0, 1));
    for (String sql : sqlDropString) {
      sessionContext.getJdbcOperations().update(sql);
    }
  }

  @Override
  public long getSequenceNextVal(String sequenceName) {
    return sessionContext.getSqlDialect().getSequenceNextVal(sequenceName, sessionContext.getJdbcOperations());
  }

  private void createIndexes(SqlTable sqlTable) {
    Iterator<SqlIndex> itr = sqlTable.getIndexIterator();
    while (itr.hasNext()) {
      SqlIndex index = itr.next();
      String[] sqlCreateString = sessionContext.getSqlDialect().getIndexExporter().getSqlCreateString(index);
      for (String sql : sqlCreateString) {
        sessionContext.getJdbcOperations().update(sql);
      }
    }
  }

  private void createUniqueKeys(SqlTable sqlTable) {
    Iterator<SqlUniqueKey> ukItr = sqlTable.getUniqueKeyIterator();
    while (ukItr.hasNext()) {
      SqlUniqueKey uniqueKey = ukItr.next();
      String[] sqlCreateString = sessionContext.getSqlDialect().getUniqueKeyExporter().getSqlCreateString(uniqueKey);
      for (String sql : sqlCreateString) {
        sessionContext.getJdbcOperations().update(sql);
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
        sessionContext.getTypeHandler(((EntityDefinition) getModel(field.getModelName())).findIdField().orElseThrow()
          .getType()).getJdbcTypeCode()
      );
      return associationColumn;
    } else if (field.isIdentity()) {
      SqlColumn idColumn = new SqlColumn();
      idColumn.setTableName(toPhysicalTableString(field.getModelName()));
      idColumn.setName(field.getName());
      idColumn.setPrimaryKey(true);
      idColumn.setUnique(true);
      idColumn.setSqlTypeCode(sessionContext.getTypeHandler(field.getType()).getJdbcTypeCode());
      DefaultValue defaultValue = field.getDefaultValue();
      idColumn.setAutoIncrement(defaultValue != null && defaultValue.isGenerated() && "autoIncrement".equals(defaultValue.getName()));
      idColumn.setComment(field.getComment());
      return idColumn;
    } else {
      SqlColumn aSqlColumn = new SqlColumn();
      aSqlColumn.setUnique(field.isUnique());
      aSqlColumn.setName(field.getName());

      aSqlColumn.setSqlTypeCode(sessionContext.getTypeHandler(field.getType()).getJdbcTypeCode());
      // fixme support large Objects
      aSqlColumn.setNullable(field.isNullable());
      aSqlColumn.setComment(field.getComment());
      aSqlColumn.setTableName(toPhysicalTableString(field.getModelName()));

      DefaultValue defaultValue = field.getDefaultValue();
      switch (field) {
        case StringField stringField -> {
          aSqlColumn.setLength(stringField.getLength());
          if (defaultValue != null && defaultValue.isFixed()) {
            aSqlColumn.setDefaultValue(defaultValue.getValue().toString());
          }
        }
        case FloatField decimalField -> {
          aSqlColumn.setPrecision(decimalField.getPrecision());
          aSqlColumn.setScale(decimalField.getScale());
          if (defaultValue != null && defaultValue.isFixed()) {
            aSqlColumn.setDefaultValue(defaultValue.getValue().toString());
          }
        }
        case JSONField jsonField -> {
          if (defaultValue != null && defaultValue.isFixed()) {
            aSqlColumn.setDefaultValue(sessionContext.getJsonObjectConverter().toJsonString(defaultValue.getValue()));
          }
        }
        case BooleanField booleanField -> {
          if (defaultValue != null && defaultValue.isFixed()) {
            aSqlColumn.setDefaultValue(sessionContext.getSqlDialect().toBooleanValueString((Boolean) defaultValue.getValue()));
          }
        }
        default -> {
          if (defaultValue != null && defaultValue.isFixed()) {
            aSqlColumn.setDefaultValue(defaultValue.getValue().toString());
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
    EntityDefinition model = (EntityDefinition) sessionContext.getModelDefinition(name);
    if (model == null) {
      return name;
    }
    return model.getName();
  }

}
