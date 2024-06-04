package tech.wetech.flexmodel.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.BasicFieldType.*;
import static tech.wetech.flexmodel.IDField.DefaultGeneratedValue.*;

/**
 * @author cjbi
 */
public class JdbcMappedModels implements MappedModels {
  public static final String STORED_TABLES = "flex_models";
  private final DataSource dataSource;
  private final SqlDialect sqlDialect;
  private final Logger log = LoggerFactory.getLogger(JdbcMappedModels.class);

  public JdbcMappedModels(DataSource dataSource) {
    this.dataSource = dataSource;
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
    try {
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery("select count(0) from " + sqlDialect.quoteIdentifier(STORED_TABLES));
      resultSet.close();
      statement.close();
      return true;
    } catch (Exception e) {
      return false;
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

    SqlColumn content = new SqlColumn();
    content.setName("content");
    content.setTableName(sqlTable.getName());
    content.setSqlTypeCode(Types.LONGVARCHAR);
    sqlTable.addColumn(content);

    SqlColumn binaryContent = new SqlColumn();
    binaryContent.setName("binary_content");
    binaryContent.setTableName(sqlTable.getName());
    binaryContent.setSqlTypeCode(Types.BLOB);
    binaryContent.setLength(Integer.MAX_VALUE);
    sqlTable.addColumn(binaryContent);

    SqlIndex sqlIndex = new SqlIndex();
    sqlIndex.setName("idx_schema_model");
    sqlIndex.setTable(sqlTable);
    sqlIndex.setUnique(true);
    sqlIndex.addColumn(schemaName);
    sqlIndex.addColumn(modelName);

    List<String> sqlList = new ArrayList<>();
    sqlList.addAll(List.of(sqlDialect.getTableExporter().getSqlCreateString(sqlTable)));
    sqlList.addAll(List.of(sqlDialect.getIndexExporter().getSqlCreateString(sqlIndex)));
    log.debug("Execute Create Sql: {}", sqlList);
    for (String sql : sqlList) {
      Statement statement = connection.createStatement();
      statement.executeUpdate(sql);
    }
  }

  @Override
  public List<Model> sync(AbstractSessionContext context) {
    SqlContext sqlContext = (SqlContext) context;
    List<Entity> entities = convert(sqlContext.getSqlMetadata().getTables(), sqlContext);
    Map<String, Model> metaMap = lookup(sqlContext.getSchemaName()).stream().collect(Collectors.toMap(Model::getName, model -> model));
    for (Entity entity : entities) {
      Model model = getIgnoreCase(entity.getName(), metaMap);
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
        sqlDialect.quoteIdentifier("flex_models"),
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
    sqlContext.getTypeHandlerMap().forEach((key, value) -> jdbcCodeMap.put(value.getJdbcTypeCode(), key));
    sqlContext.getTypeHandlerMap().forEach((key, value) -> jdbcCodeMap.put(value.getJdbcTypeCode(), key));
    for (SqlTable table : tables) {
      Entity entity = new Entity(table.getName());
      entity.setComment(table.getComment());
      Iterator<SqlColumn> colIte = table.getColumnIterator();
      while (colIte.hasNext()) {
        SqlColumn sqlColumn = colIte.next();
        BasicFieldType fieldType = sqlColumn.isPrimaryKey() ? ID
          : BasicFieldType.fromType(jdbcCodeMap.getOrDefault(sqlColumn.getSqlTypeCode(), STRING.getType()));
        assert fieldType != null;
        TypedField<?, ?> field;
        switch (fieldType) {
          case ID: {
            IDField idField = new IDField(sqlColumn.getName());
            field = idField;
            if (sqlColumn.isAutoIncrement()) {
              idField.setGeneratedValue(IDENTITY);
            }
            BasicFieldType idType = BasicFieldType.fromType(jdbcCodeMap.getOrDefault(sqlColumn.getSqlTypeCode(), STRING.getType()));
            idField.setGeneratedValue(idType == BIGINT || idType == INT || idType == DECIMAL ? BIGINT_NO_GEN : STRING_NO_GEN);
            break;
          }
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
          case TEXT: {
            TextField textField = new TextField(sqlColumn.getName());
            field = textField;
            if (sqlColumn.getDefaultValue() != null) {
              try {
                textField.setDefaultValue(sqlColumn.getDefaultValue());
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
          case DECIMAL: {
            DecimalField decimalField = new DecimalField(sqlColumn.getName());
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
          case BIGINT: {
            BigintField bigintField = new BigintField(sqlColumn.getName());
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
            DatetimeField datetimeField = new DatetimeField(sqlColumn.getName());
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
            JsonField jsonField = new JsonField(sqlColumn.getName());
            field = jsonField;
            if (sqlColumn.getDefaultValue() != null) {
              try {
                jsonField.setDefaultValue(JsonUtils.getInstance().parseToObject(sqlColumn.getDefaultValue(), Serializable.class));
              } catch (Exception e) {
                log.warn("Unexpected default value: {}, message: {}", sqlColumn.getDefaultValue(), e.getMessage());
              }
            }
            break;
          }
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
  public List<Model> lookup(String schemaName) {
    try (Connection connection = dataSource.getConnection()) {
      NamedParameterSqlExecutor sqlExecutor = new NamedParameterSqlExecutor(connection);
      String sqlSelectString = "select " + sqlDialect.quoteIdentifier("binary_content") +
                               " \nfrom " + sqlDialect.quoteIdentifier(STORED_TABLES) +
                               " \nwhere " + sqlDialect.quoteIdentifier("schema_name") + "=:schemaName";
      List<Map<String, Object>> mapList = sqlExecutor.queryForList(sqlSelectString, Map.of("schemaName", schemaName));
      List<Model> result = new ArrayList<>();
      for (Map<String, Object> data : mapList) {
        Object binaryContent = data.get("binary_content");
        if (binaryContent instanceof Blob blob) {
          result.add((Model) deserialize(blob.getBinaryStream().readAllBytes()));
        } else if (binaryContent instanceof byte[] bytes) {
          result.add((Model) deserialize(bytes));
        } else {
          throw new RuntimeException("get model error, unknown data type:" + binaryContent.getClass());
        }
      }
      return result;
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
  public void persist(String schemaName, Model model) {
    try (Connection connection = dataSource.getConnection()) {
      String content = JsonUtils.getInstance().stringify(model);
      log.trace("Persist:\n{}", content);
      connection.setAutoCommit(false);
      NamedParameterSqlExecutor sqlExecutor = new NamedParameterSqlExecutor(connection);
      String sqlDeleteString = getDeleteString();
      sqlExecutor.update(sqlDeleteString, Map.of("schemaName", schemaName, "modelName", model.getName()));
      String sqlInsertString = "insert into " + sqlDialect.quoteIdentifier("flex_models") +
                               " values (:schemaName, :modelName, :content, :binaryContent)\n";
      sqlExecutor.update(sqlInsertString, Map.of("schemaName", schemaName
        , "modelName", model.getName()
        , "content", content
        , "binaryContent", serialize(model)
      ));
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
  public Model getModel(String schemaName, String modelName) {
    try (Connection connection = dataSource.getConnection()) {
      NamedParameterSqlExecutor sqlExecutor = new NamedParameterSqlExecutor(connection);
      String sqlSelectString = "select " + sqlDialect.quoteIdentifier("binary_content") +
                               " \nfrom " + sqlDialect.quoteIdentifier("flex_models") +
                               " \nwhere " + sqlDialect.quoteIdentifier("schema_name") + "=:schemaName and " + sqlDialect.quoteIdentifier("model_name") + "=:modelName";
      Map<String, Object> map = sqlExecutor.queryForMap(sqlSelectString,
        Map.of("schemaName", schemaName, "modelName", modelName));
      if (map == null) {
        return null;
      }
      Object binaryContent = map.get("binary_content");

      if (binaryContent instanceof Blob blob) {
        return (Model) deserialize(blob.getBinaryStream().readAllBytes());
      } else if (binaryContent instanceof byte[] bytes) {
        return (Model) deserialize(bytes);
      } else {
        throw new RuntimeException("get model error, unknown data type:" + binaryContent.getClass());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private byte[] serialize(final Object obj) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
      out.writeObject(obj);
      out.flush();
      return bos.toByteArray();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private Object deserialize(byte[] bytes) {
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    try (ObjectInput in = new ObjectInputStream(bis)) {
      return in.readObject();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
