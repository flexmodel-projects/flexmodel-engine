package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.conversions.Bson;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.sql.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
public class MongoSchemaOperations extends BaseMongoStatement implements SchemaOperations {

  private final String schemaName;
  private final MongoDatabase mongoDatabase;
  private final MappedModels mappedModels;

  public MongoSchemaOperations(MongoContext mongoContext) {
    super(mongoContext);
    this.schemaName = mongoContext.getSchemaName();
    this.mongoDatabase = mongoContext.getMongoDatabase();
    this.mappedModels = mongoContext.getMappedModels();
  }

  @Override
  public List<Model> syncModels() {
    return mongoContext.getMappedModels().sync(mongoContext);
  }

  @Override
  public List<Model> syncModels(Set<String> modelNames) {
    return mongoContext.getMappedModels().sync(mongoContext, modelNames);
  }

  @Override
  public List<Model> getAllModels() {
    return mappedModels.lookup(mongoContext.getSchemaName());
  }

  @Override
  public Model getModel(String modelName) {
    return mappedModels.getModel(schemaName, modelName);
  }

  @Override
  public void dropModel(String modelName) {
    String collectionName = getCollectionName(modelName);
    mongoDatabase.getCollection(collectionName).drop();
  }

  @Override
  public Entity createCollection(Entity collection) {
    String collectionName = getCollectionName(collection.getName());
    mongoDatabase.createCollection(collectionName);
    for (Index index : collection.getIndexes()) {
      createIndex(index);
    }
    collection.findIdField().ifPresent(idField -> {
      Index index = new Index(idField.getModelName());
      index.setUnique(true);
      index.addField(idField.getName());
      createIndex(index);
    });
    return collection;
  }

  @Override
  public NativeQueryModel createNativeQueryModel(NativeQueryModel model) {
    return model;
  }

  @Override
  public TypedField<?, ?> createField(TypedField<?, ?> field) {
    if (field instanceof IDField) {
      Index index = new Index(field.getModelName());
      index.setUnique(true);
      index.addField(field.getName());
      createIndex(index);
    }
    return field;
  }

  @Override
  public TypedField<?, ?> modifyField(TypedField<?, ?> field) {
    // mongodb无需修改schema
    return field;
  }

  @Override
  public void dropField(String modelName, String fieldName) {
    // ignored
  }

  @Override
  public Index createIndex(Index index) {
    String collectionName = getCollectionName(index.getModelName());
    List<Bson> indexes = new ArrayList<>();
    for (Index.Field field : index.getFields()) {
      indexes.add(field.direction() == Direction.ASC
        ? Indexes.ascending(field.fieldName())
        : Indexes.descending(field.fieldName())
      );
    }
    IndexOptions indexOptions = new IndexOptions();
    indexOptions.name(getPhysicalIndexName(index.getModelName(), index.getName()));
    indexOptions.unique(index.isUnique());
    mongoDatabase.getCollection(collectionName).createIndex(Indexes.compoundIndex(indexes), indexOptions);
    return index;
  }

  private String getPhysicalIndexName(String modelName, String indexName) {
    return indexName != null ? indexName : "IDX_" + StringHelper.hashedName(modelName + System.currentTimeMillis());
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    String collectionName = getCollectionName(modelName);
    mongoDatabase.getCollection(collectionName).dropIndex(getPhysicalIndexName(modelName, indexName));
  }

  @Override
  public void createSequence(String sequenceName, int initialValue, int incrementSize) {
    String collectionName = getCollectionName("flex_sequences");
    mongoDatabase.getCollection(collectionName).insertOne(Document.parse(String.format("""
      {
        _id: "%s",
        seq: %s
      }
      """, sequenceName, initialValue)));
  }

  private String getCollectionName(String modelName) {
    Entity model = (Entity) mongoContext.getModel(modelName);
    if (model == null) {
      return modelName;
    }
    return model.getName();
  }

  @Override
  public void dropSequence(String sequenceName) {
    String collectionName = getCollectionName(sequenceName);
    mongoDatabase.getCollection(collectionName).deleteOne(Filters.eq("_id", sequenceName));
  }

  @Override
  public long getSequenceNextVal(String sequenceName) {
    String collectionName = getCollectionName("flex_sequences");
    Document document = mongoDatabase.getCollection(collectionName)
      .findOneAndUpdate(Filters.eq("_id", sequenceName), Document.parse("{ $inc: { seq: 1 } }"));
    assert document != null;
    return ((Number) document.get("seq")).longValue();
  }

}
