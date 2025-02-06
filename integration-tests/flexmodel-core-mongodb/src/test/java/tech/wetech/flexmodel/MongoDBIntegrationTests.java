package tech.wetech.flexmodel;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tech.wetech.flexmodel.mongodb.MongoDataSourceProvider;

import java.util.List;
import java.util.Map;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * @author cjbi
 */
@Testcontainers
public class MongoDBIntegrationTests extends AbstractSessionTests {

  @Container
  public static MongoDBContainer container = new MongoDBContainer("mongo:5.0.0");

  @BeforeAll
  public static void beforeAll() {
    CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
      fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    MongoClient mongoClient = MongoClients.create(container.getConnectionString());
    MongoDatabase database = mongoClient.getDatabase("test")
      .withCodecRegistry(pojoCodecRegistry);
    initSession(new MongoDataSourceProvider("default", database));
  }

  @Test
  void testNativeQuery() {
    String classesEntityName = "TestNativeQueryClasses";
    createClassesEntity(classesEntityName);
    createClassesData(classesEntityName);
    List<Map> list = session.findByNativeQuery(
      """
        {
            "find": "TestNativeQueryClasses",
            "filter": { "id": ${id}, "className": "${className}"},
            "sort": {"id": -1},
            "limit": 10
        }
        """,
      Map.of("id", 3,
        "className", "二年级1班"), Map.class);
    Assertions.assertFalse(list.isEmpty());
  }

  @Test
  void testNativeQueryModel() {
    String classesEntityName = "TestNativeQueryModelClasses";
    createClassesEntity(classesEntityName);
    createClassesData(classesEntityName);
    List<Map> list = session.findByNativeQuery(
      """
        {
            "find": "TestNativeQueryModelClasses",
            "filter": { "id": ${id}, "className": "${className}"},
            "sort": {"id": -1},
            "limit": 10
        }
        """,
      Map.of("id", 3,
        "className", "二年级1班"), Map.class);
    Assertions.assertFalse(list.isEmpty());
  }

}
