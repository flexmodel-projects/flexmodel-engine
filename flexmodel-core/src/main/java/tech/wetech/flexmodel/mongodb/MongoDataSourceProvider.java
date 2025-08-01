package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import tech.wetech.flexmodel.DataSourceProvider;

/**
 * @author cjbi
 */
public record MongoDataSourceProvider(String id, MongoDatabase mongoDatabase) implements DataSourceProvider {
  @Override
  public String getId() {
    return id;
  }
}
