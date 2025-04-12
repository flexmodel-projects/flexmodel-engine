package tech.wetech.flexmodel.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.GeneratedValue.AUTO_INCREMENT;
import static tech.wetech.flexmodel.ScalarType.STRING;

/**
 * @author cjbi
 */
public class JdbcMappedModels implements MappedModels {
  public static final String STORED_TABLES = "fe_models";
  private final DataSource dataSource;
  private final SqlDialect sqlDialect;
  private final Logger log = LoggerFactory.getLogger(JdbcMappedModels.class);
  private final JsonObjectConverter jsonObjectConverter;

  public JdbcMappedModels(DataSource dataSource, JsonObjectConverter jsonObjectConverter) {
    this.dataSource = dataSource;
    this.jsonObjectConverter = jsonObjectConverter;
    try (Connection connection = dataSource.getConnection()) {
      sqlDialect = SqlDialectFactory.create(dataSource.getConnection().getMetaData());
      if (!existSchema(connection)) {
        initSchema(connection);
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

  }

  private boolean existSchema(Connection connection) {
    Statement statement = null;
    ResultSet resultSet = null;
    try {
      statement = connection.createStatement();
      resultSet = statement.executeQuery("select count(0) from " + sqlDialect.quoteIdentifier(STORED_TABLES));
      return true;
    } catch (Exception e) {
      return false;
    } finally {
      closeStatement(statement);
      closeResultSet(resultSet);
    }
  }

  private void closeResultSet(ResultSet resultSet) {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void closeStatement(Statement statement) {
    if (statement != null) {
      try {
        statement.close();
      } catch (SQLException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void initSchema(Connection connection) throws SQLException {
    SqlTable sqlTable = new SqlTable();
    sqlTable.setName(STORED_TABLES);

    SqlColumn schemaName = new SqlColumn();
    schemaName.setName("schema_name");
    schemaName.setTableName(sqlTable.getName());
    schemaName.setSqlTypeCode(Types.VARCHAR);
    schemaName.setLength(100);
    sqlTable.addColumn(schemaName);

    SqlColumn modelName = new SqlColumn();
    modelName.setName("model_name");
    modelName.setTableName(sqlTable.getName());
    modelName.setSqlTypeCode(Types.VARCHAR);
    modelName.setLength(100);
    sqlTable.addColumn(modelName);

    SqlColumn modelType = new SqlColumn();
    modelType.setName("model_type");
    modelType.setTableName(sqlTable.getName());
    modelType.setSqlTypeCode(Types.VARCHAR);
    modelType.setLength(100);
    sqlTable.addColumn(modelType);

    SqlColumn content = new SqlColumn();
    content.setName("content");
    content.setTableName(sqlTable.getName());
    content.setSqlTypeCode(Types.JAVA_OBJECT);
    sqlTable.addColumn(content);

    SqlIndex idxSchema = new SqlIndex();
    idxSchema.setName("idx_schema");
    idxSchema.setTable(sqlTable);
    idxSchema.setUnique(false);
    idxSchema.addColumn(schemaName);

    SqlIndex idxSchemaModel = new SqlIndex();
    idxSchemaModel.setName("idx_schema_model");
    idxSchemaModel.setTable(sqlTable);
    idxSchemaModel.setUnique(true);
    idxSchemaModel.addColumn(schemaName);
    idxSchemaModel.addColumn(modelName);

    List<String> sqlList = new ArrayList<>();
    sqlList.addAll(List.of(sqlDialect.getTableExporter().getSqlCreateString(sqlTable)));
    sqlList.addAll(List.of(sqlDialect.getIndexExporter().getSqlCreateString(idxSchema)));
    sqlList.addAll(List.of(sqlDialect.getIndexExporter().getSqlCreateString(idxSchemaModel)));
    log.debug("Execute Create Sql: {}", sqlList);
    for (String sql : sqlList) {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate(sql);
      }
    }
  }

  @Override
  public List<SchemaObject> sync(AbstractSessionContext context) {
    return sync(context, null);
  }

  @Override
  public List<SchemaObject> sync(AbstractSessionContext context, Set<String> includes) {
    SqlContext sqlContext = (SqlContext) context;
    List<Entity> entities = convert(sqlContext.getSqlMetadata().getTables(includes), sqlContext);
    Map<String, SchemaObject> metaMap = lookup(sqlContext.getSchemaName()).stream().collect(Collectors.toMap(SchemaObject::getName, wrapper -> wrapper));
    for (Entity entity : entities) {
      SchemaObject model = getIgnoreCase(entity.getName(), metaMap);
      if (model == null) {
        this.persist(sqlContext.getSchemaName(), entity);
        metaMap.put(entity.getName(), entity);
      }
    }
    Set<String> diff = new HashSet<>();
    Map<String, Entity> entityMap = entities.stream().collect(Collectors.toMap(Model::getName, model -> model));
    for (String key : metaMap.keySet()) {
      if (getIgnoreCase(key, entityMap) == null) {
        diff.add(key);
      }
    }
    if (!diff.isEmpty()) {
      deleteRedundant(sqlContext.getSchemaName(), diff);
    }
    return new ArrayList<>(entities);
  }

  private <T> T getIgnoreCase(String compareKey, Map<String, T> data) {
    Set<Map.Entry<String, T>> entries = data.entrySet();
    for (Map.Entry<String, T> entry : entries) {
      String key = entry.getKey();
      if (key.equalsIgnoreCase(compareKey)) {
        return entry.getValue();
      }
    }
    return null;
  }

  private void deleteRedundant(String schemaName, Set<String> modelNames) {

    try (Connection connection = dataSource.getConnection()) {
      Map<String, Object> paramMap = new HashMap<>();
      paramMap.put("schemaName", schemaName);
      StringJoiner sqlIn = new StringJoiner(", ");
      int index = 0;
      for (String modelName : modelNames) {
        String named = "modelName_" + (index++);
        paramMap.put(named, modelName);
        sqlIn.add(":" + named);
      }
      String sqlDeleteString = MessageFormat.format(" \ndelete from {0} \nwhere {1}=:schemaName and {2} in ({3})",
        sqlDialect.quoteIdentifier(STORED_TABLES),
        sqlDialect.quoteIdentifier("schema_name"),
        sqlDialect.quoteIdentifier("model_name"),
        sqlIn
      );
      NamedParameterSqlExecutor sqlExecutor = new NamedParameterSqlExecutor(connection);
      sqlExecutor.update(sqlDeleteString, paramMap);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  private List<Entity> convert(List<SqlTable> tables, SqlContext sqlContext) {
    List<Entity> modelList = new ArrayList<>();
    Map<Integer, String> jdbcCodeMap = new HashMap<>();
    sqlContext.getTypeHandlerMap().forEach((key, value) -> {
      if (!jdbcCodeMap.containsKey(value.getJdbcTypeCode())) {
        jdbcCodeMap.put(value.getJdbcTypeCode(), key);
      }
    });
    for (SqlTable table : tables) {
      Entity entity = new Entity(table.getName());
      entity.setComment(table.getComment());
      Iterator<SqlColumn> colIte = table.getColumnIterator();
      while (colIte.hasNext()) {
        SqlColumn sqlColumn = colIte.next();
        ScalarType fieldType = ScalarType.fromType(jdbcCodeMap.getOrDefault(sqlColumn.getSqlTypeCode(), STRING.getType()));
        assert fieldType != null;
        TypedField<?, ?> field;
        switch (fieldType) {
          default:
          case STRING: {
            StringField stringField = new StringField(sqlColumn.getName());
            field = stringField;
            if (sqlColumn.getDefaultValue() != null) {
              try {
                stringField.setDefaultValue(sqlColumn.getDefaultValue());
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            stringField.setLength(sqlColumn.getLength());
            break;
          }
          case FLOAT: {
            FloatField decimalField = new FloatField(sqlColumn.getName());
            field = decimalField;
            decimalField.setPrecision(sqlColumn.getPrecision());
            decimalField.setScale(sqlColumn.getScale());
            if (sqlColumn.getDefaultValue() != null) {
              try {
                decimalField.setDefaultValue(Double.valueOf(sqlColumn.getDefaultValue()));
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
          case INT: {
            IntField intField = new IntField(sqlColumn.getName());
            field = intField;
            if (sqlColumn.getDefaultValue() != null) {
              try {
                intField.setDefaultValue(Integer.valueOf(sqlColumn.getDefaultValue()));
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
          case LONG: {
            LongField bigintField = new LongField(sqlColumn.getName());
            field = bigintField;
            if (sqlColumn.getDefaultValue() != null && !sqlColumn.getDefaultValue().equals("NULL")) {
              try {
                bigintField.setDefaultValue(Long.valueOf(sqlColumn.getDefaultValue()));
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
          case BOOLEAN: {
            BooleanField booleanField = new BooleanField(sqlColumn.getName());
            field = booleanField;
            if (sqlColumn.getDefaultValue() != null) {
              try {
                booleanField.setDefaultValue(Boolean.valueOf(sqlColumn.getDefaultValue()));
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
          case DATETIME: {
            DateTimeField datetimeField = new DateTimeField(sqlColumn.getName());
            field = datetimeField;
            if (sqlColumn.getDefaultValue() != null) {
              try {
                datetimeField.setDefaultValue(LocalDateTime.parse(sqlColumn.getDefaultValue()));
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
          case TIME: {
            TimeField timeField = new TimeField(sqlColumn.getName());
            field = timeField;
            if (sqlColumn.getDefaultValue() != null) {
              try {
                timeField.setDefaultValue(LocalTime.parse(sqlColumn.getDefaultValue()));
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
          case DATE: {
            DateField dateField = new DateField(sqlColumn.getName());
            field = dateField;
            if (sqlColumn.getDefaultValue() != null) {
              try {
                dateField.setDefaultValue(LocalDate.parse(sqlColumn.getDefaultValue()));
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
          case JSON: {
            JSONField jsonField = new JSONField(sqlColumn.getName());
            field = jsonField;
            if (sqlColumn.getDefaultValue() != null) {
              try {
                jsonField.setDefaultValue(sqlContext.getJsonObjectConverter().parseToObject(sqlColumn.getDefaultValue(), Serializable.class));
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
        }
        field.setIdentity(sqlColumn.isPrimaryKey());
        if (sqlColumn.isAutoIncrement()) {
          field.setDefaultValue(AUTO_INCREMENT);
        }
        field.setModelName(sqlColumn.getTableName());
        field.setComment(sqlColumn.getComment());
        field.setUnique(sqlColumn.isUnique());
        field.setNullable(sqlColumn.isNullable());
        entity.addField(field);
      }
      Iterator<SqlIndex> idxIte = table.getIndexIterator();
      while (idxIte.hasNext()) {
        SqlIndex sqlIndex = idxIte.next();
        Index index = new Index(table.getName());
        index.setName(sqlIndex.getName());
        index.setUnique(sqlIndex.isUnique());
        List<SqlColumn> columns = sqlIndex.getColumns();
        Map<SqlColumn, String> columnOrderMap = sqlIndex.getColumnOrderMap();
        for (SqlColumn column : columns) {
          String order = columnOrderMap.get(column);
          if (order != null) {
            index.addField(column.getName(), Direction.valueOf(order));
          } else {
            index.addField(column.getName());
          }
        }
        entity.addIndex(index);
      }
      modelList.add(entity);
    }
    return modelList;
  }

  @Override
  public List<SchemaObject> lookup(String schemaName) {

    try (Connection connection = dataSource.getConnection()) {
      NamedParameterSqlExecutor sqlExecutor = new NamedParameterSqlExecutor(connection);
      String sqlSelectString = "select " + sqlDialect.quoteIdentifier("content") +
                               " \nfrom " + sqlDialect.quoteIdentifier(STORED_TABLES) +
                               " \nwhere " + sqlDialect.quoteIdentifier("schema_name") + "=:schemaName";
      List<Map<String, Object>> mapList = sqlExecutor.queryForList(sqlSelectString, Map.of("schemaName", schemaName));
      List<SchemaObject> result = new ArrayList<>();
      for (Map<String, Object> data : mapList) {
        Object content = data.get("content");
        try {
          switch (content) {
            case String contentStr -> result.add(jsonObjectConverter.parseToObject(contentStr, SchemaObject.class));
            case Blob blob ->
              result.add(jsonObjectConverter.parseToObject(Arrays.toString(blob.getBinaryStream().readAllBytes()), SchemaObject.class));
            case byte[] bytes ->
              result.add(jsonObjectConverter.parseToObject(Arrays.toString(bytes), SchemaObject.class));
            case null, default -> {
              assert content != null;
              throw new RuntimeException("get model error, unknown data type:" + content.getClass());
            }
          }
        } catch (Exception e) {
          log.error("parse model error, schemaName:{}, data:{}, message: {}", schemaName, data, e.getMessage());
        }

      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void removeAll(String schemaName) {
    try (Connection connection = dataSource.getConnection()) {
      NamedParameterSqlExecutor sqlExecutor = new NamedParameterSqlExecutor(connection);
      String sqlDeleteString = "delete from " + sqlDialect.quoteIdentifier(STORED_TABLES) +
                               " \nwhere " + sqlDialect.quoteIdentifier("schema_name") + "=:schemaName and ";
      sqlExecutor.update(sqlDeleteString, Map.of("schemaName", schemaName));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void remove(String schemaName, String modelName) {
    try (Connection connection = dataSource.getConnection()) {
      NamedParameterSqlExecutor sqlExecutor = new NamedParameterSqlExecutor(connection);
      String sqlDeleteString = getDeleteString();
      sqlExecutor.update(sqlDeleteString, Map.of("schemaName", schemaName, "modelName", modelName));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void persist(String schemaName, SchemaObject object) {
    try (Connection connection = dataSource.getConnection()) {
      String content = jsonObjectConverter.toJsonString(object);
      log.trace("Persist:\n{}", content);
      connection.setAutoCommit(false);
      NamedParameterSqlExecutor sqlExecutor = new NamedParameterSqlExecutor(connection);
      SchemaObject older;
      if ((older = this.getModel(schemaName, object.getName())) != null) {
        String oldContent = jsonObjectConverter.toJsonString(older);
        if (!oldContent.equals(content)) {
          String updateString = "update " + sqlDialect.quoteIdentifier(STORED_TABLES) + "set " + sqlDialect.quoteIdentifier("content") + "=:content" +
                                " \nwhere " + sqlDialect.quoteIdentifier("schema_name") + "=:schemaName and " + sqlDialect.quoteIdentifier("model_name") + "=:modelName";
          sqlExecutor.update(
            updateString,
            Map.of("schemaName", schemaName,
              "modelName", object.getName(),
              "content", content));
        }
      } else {
        String sqlInsertString = "insert into " + sqlDialect.quoteIdentifier(STORED_TABLES) +
                                 " values (:schemaName, :modelName, :modelType, :content)\n";
        sqlExecutor.update(sqlInsertString,
          Map.of("schemaName", schemaName,
            "modelName", object.getName(),
            "modelType", object.getType(),
            "content", content
          ));
      }
      connection.commit();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getDeleteString() {
    return "delete from " + sqlDialect.quoteIdentifier(STORED_TABLES) +
           " \nwhere " + sqlDialect.quoteIdentifier("schema_name") + "=:schemaName and " + sqlDialect.quoteIdentifier("model_name") + "=:modelName";
  }

  @Override
  public SchemaObject getModel(String schemaName, String modelName) {
    try (Connection connection = dataSource.getConnection()) {
      NamedParameterSqlExecutor sqlExecutor = new NamedParameterSqlExecutor(connection);
      String sqlSelectString = "select " + sqlDialect.quoteIdentifier("content") +
                               " \nfrom " + sqlDialect.quoteIdentifier(STORED_TABLES) +
                               " \nwhere " + sqlDialect.quoteIdentifier("schema_name") + "=:schemaName and " + sqlDialect.quoteIdentifier("model_name") + "=:modelName";
      String content = sqlExecutor.queryForScalar(sqlSelectString,
        Map.of("schemaName", schemaName, "modelName", modelName), String.class);
      return jsonObjectConverter.parseToObject(content, SchemaObject.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
