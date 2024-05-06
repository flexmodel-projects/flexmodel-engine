package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import tech.wetech.flexmodel.DataSourceProvider;

/**
 * @author cjbi
 */
public record MongoDataSourceProvider(MongoDatabase mongoDatabase) implements DataSourceProvider {
}
