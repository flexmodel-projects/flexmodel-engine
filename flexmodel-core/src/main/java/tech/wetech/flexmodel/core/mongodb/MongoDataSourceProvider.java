package tech.wetech.flexmodel.core.mongodb;

import com.mongodb.client.MongoDatabase;
import tech.wetech.flexmodel.core.DataSourceProvider;

/**
 * @author cjbi
 */
public record MongoDataSourceProvider(String id, MongoDatabase mongoDatabase) implements DataSourceProvider {
  @Override
  public String getId() {
    return id;
  }
}
