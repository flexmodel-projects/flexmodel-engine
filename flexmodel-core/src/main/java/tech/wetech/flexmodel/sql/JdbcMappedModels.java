package tech.wetech.flexmodel.sql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.JsonUtils;
import tech.wetech.flexmodel.MappedModels;
import tech.wetech.flexmodel.Model;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import javax.sql.DataSource;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
