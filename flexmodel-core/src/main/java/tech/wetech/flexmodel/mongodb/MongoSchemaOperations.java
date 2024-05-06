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
import java.util.function.UnaryOperator;

/**
 * @author cjbi
 */
public class MongoSchemaOperations implements SchemaOperations {

  private final MongoContext mongoContext;
  private final String schemaName;
  private final MongoDatabase mongoDatabase;
  private final MappedModels mappedModels;
  private final PhysicalNamingStrategy physicalNamingStrategy;

  public MongoSchemaOperations(MongoContext mongoContext) {
    this.mongoContext = mongoContext;
    this.schemaName = mongoContext.getSchemaName();
    this.mongoDatabase = mongoContext.getMongoDatabase();
    this.mappedModels = mongoContext.getMappedModels();
    this.physicalNamingStrategy = mongoContext.getPhysicalNamingStrategy();
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
  public Entity createEntity(String modelName, UnaryOperator<Entity> entityUnaryOperator) {
    Entity entity = new Entity(modelName);
    entityUnaryOperator.apply(entity);
    String collectionName = getCollectionName(entity.name());
    mongoDatabase.createCollection(collectionName);
    for (Index index : entity.indexes()) {
      createIndex(index);
    }
    return entity;
  }

  @Override
  public View createView(String viewName, String viewOn, UnaryOperator<Query> queryUnaryOperator) {
    Query query = new Query();
    queryUnaryOperator.apply(query);
    mongoDatabase.createView(viewName, viewOn, MongoHelper.createPipeline(viewName, mongoContext, query));
    View view = new View(viewName);
    view.setViewOn(viewOn);
    view.setQuery(query);
    return view;
  }

  @Override
  public void createField(String modelName, TypedField<?, ?> field) {
    field.setModelName(modelName);
    if (field instanceof IDField) {
      Index index = new Index(field.modelName());
      index.addField(field.name());
      createIndex(index);
    }
  }

  @Override
  public void dropField(String entityName, String fieldName) {
    // ignored
  }

  @Override
  public void createIndex(Index index) {
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
    return physicalNamingStrategy.toPhysicalSequenceName(modelName);
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
