package tech.wetech.flexmodel;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tech.wetech.flexmodel.sql.JdbcDataSourceProvider;

import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class SQLiteIntegrationTests extends AbstractSessionTests {

  @BeforeAll
  public static void beforeAll() {
    HikariDataSource dataSource = new HikariDataSource();
    dataSource.setJdbcUrl("jdbc:sqlite:file::memory:?cache=shared");
    initSession(new JdbcDataSourceProvider("default", dataSource));
  }

  @Test
  void testNativeQuery() {
    String classesEntityName = "testNativeQueryClasses";
    createClassesEntity(classesEntityName);
    createClassesData(classesEntityName);
    List<Map> list = session.findByNativeQuery(
      "select * from " + classesEntityName + " where id=${id} and className=${className} limit 10",
      Map.of("id", 3,
        "className", "二年级1班"), Map.class);
    Assertions.assertFalse(list.isEmpty());
  }

  @Test
  void testNativeQueryModel() {
    String classesEntityName = "testNativeQueryModelClasses";
    createClassesEntity(classesEntityName);
    createClassesData(classesEntityName);
    String name = "testNativeQueryModel";
    NativeQueryDefinition model = new NativeQueryDefinition(name);
    model.setStatement("select * from " + classesEntityName + " where id=${id} and className=${className} limit 10");
    session.createNativeQueryModel(model);
    List<Map> list = session.findByNativeQueryModel(name, Map.of("id", 3, "className", "二年级1班"), Map.class);
    Assertions.assertFalse(list.isEmpty());
//    Assertions.assertNotNull(session.getAllModels());
  }

}
