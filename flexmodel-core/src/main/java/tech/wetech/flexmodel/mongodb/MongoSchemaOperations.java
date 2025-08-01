package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.conversions.Bson;
import tech.wetech.flexmodel.ModelRepository;
import tech.wetech.flexmodel.model.*;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.operation.SchemaOperations;
import tech.wetech.flexmodel.query.Direction;
import tech.wetech.flexmodel.sql.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cjbi
 */
public class MongoSchemaOperations extends BaseMongoStatement implements SchemaOperations {

  private final String schemaName;
  private final MongoDatabase mongoDatabase;
  private final ModelRepository mappedModels;

  public MongoSchemaOperations(MongoContext mongoContext) {
    super(mongoContext);
    this.schemaName = mongoContext.getSchemaName();
    this.mongoDatabase = mongoContext.getMongoDatabase();
    this.mappedModels = mongoContext.getMappedModels();
  }

  @Override
  public List<SchemaObject> syncModels() {
    return mongoContext.getMappedModels().syncFromDatabase(mongoContext);
  }

  @Override
  public List<SchemaObject> syncModels(Set<String> modelNames) {
    return mongoContext.getMappedModels().syncFromDatabase(mongoContext, modelNames);
  }

  @Override
  public List<SchemaObject> getAllModels() {
    return mappedModels.findAll(mongoContext.getSchemaName());
  }

  @Override
  public SchemaObject getModel(String modelName) {
    return mappedModels.find(schemaName, modelName);
  }

  @Override
  public void dropModel(String modelName) {
    String collectionName = getCollectionName(modelName);
    mongoDatabase.getCollection(collectionName).drop();
  }

  @Override
  public EntityDefinition createEntity(EntityDefinition collection) {
    String collectionName = getCollectionName(collection.getName());
    mongoDatabase.createCollection(collectionName);
    for (IndexDefinition index : collection.getIndexes()) {
      createIndex(index);
    }
    collection.findIdField().ifPresent(idField -> {
      IndexDefinition index = new IndexDefinition(idField.getModelName());
      index.setUnique(true);
      index.addField(idField.getName());
      createIndex(index);
    });
    return collection;
  }

  @Override
  public NativeQueryDefinition createNativeQueryModel(NativeQueryDefinition model) {
    return model;
  }

  @Override
  public EnumDefinition createEnum(EnumDefinition anEnum) {
    return anEnum;
  }

  @Override
  public TypedField<?, ?> createField(TypedField<?, ?> field) {
    if (field.isIdentity()) {
      IndexDefinition index = new IndexDefinition(field.getModelName());
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
  public IndexDefinition createIndex(IndexDefinition index) {
    String collectionName = getCollectionName(index.getModelName());
    List<Bson> indexes = new ArrayList<>();
    for (IndexDefinition.Field field : index.getFields()) {
      indexes.add(field.direction() == Direction.ASC
        ? Indexes.ascending(field.fieldName())
        : Indexes.descending(field.fieldName())
      );
    }
    IndexOptions indexOptions = new IndexOptions();
    indexOptions.name(getPhysicalIndexName(index));
    indexOptions.unique(index.isUnique());
    mongoDatabase.getCollection(collectionName).createIndex(Indexes.compoundIndex(indexes), indexOptions);
    return index;
  }

  private String getPhysicalIndexName(IndexDefinition index) {
    String modelName = index.getModelName();
    String indexName = index.getName();

    return indexName != null ? indexName : "IDX_" + StringHelper.hashedName(modelName + index.getFields().stream()
      .map(IndexDefinition.Field::fieldName)
      .collect(Collectors.joining())
    );
  }

  @Override
  public void dropIndex(String modelName, String indexName) {
    String collectionName = getCollectionName(modelName);
    mongoDatabase.getCollection(collectionName).dropIndex(indexName);
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
    EntityDefinition model = (EntityDefinition) mongoContext.getModel(modelName);
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
