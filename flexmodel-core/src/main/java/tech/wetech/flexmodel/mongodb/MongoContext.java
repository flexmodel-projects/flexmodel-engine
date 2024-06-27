package tech.wetech.flexmodel.mongodb;

import com.mongodb.client.MongoDatabase;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.mapping.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class MongoContext extends AbstractSessionContext {

  private final MongoDatabase mongoDatabase;
  private ExpressionCalculator<String> conditionCalculator;
  private final Map<String, TypeHandler<?>> typeHandlerMap = new HashMap<>();

  public MongoContext(String schemaName, MongoDatabase mongoDatabase, MappedModels mappedModels, JsonObjectConverter jsonObjectConverter) {
    super(schemaName, mappedModels, jsonObjectConverter);
    this.mongoDatabase = mongoDatabase;
    this.conditionCalculator = new DefaultMongoExpressionCalculator();

    this.typeHandlerMap.put(BasicFieldType.STRING.getType(), new StringTypeHandler());
    this.typeHandlerMap.put(BasicFieldType.TEXT.getType(), new TextTypeHandler());
    this.typeHandlerMap.put(BasicFieldType.DECIMAL.getType(), new DecimalTypeHandler());
    this.typeHandlerMap.put(BasicFieldType.INT.getType(), new IntTypeHandler());
    this.typeHandlerMap.put(BasicFieldType.BIGINT.getType(), new BigintTypeHandler());
    this.typeHandlerMap.put(BasicFieldType.BOOLEAN.getType(), new BooleanTypeHandler());
    this.typeHandlerMap.put(BasicFieldType.DATETIME.getType(), new DatetimeTypeHandler());
    this.typeHandlerMap.put(BasicFieldType.DATE.getType(), new DateTypeHandler());
    this.typeHandlerMap.put(BasicFieldType.JSON.getType(), new JsonTypeHandler(jsonObjectConverter));
  }

  public MongoDatabase getMongoDatabase() {
    return mongoDatabase;
  }

  public ExpressionCalculator<String> getConditionCalculator() {
    return conditionCalculator;
  }

  public void setConditionCalculator(ExpressionCalculator<String> conditionCalculator) {
    this.conditionCalculator = conditionCalculator;
  }

  @Override
  public Map<String, TypeHandler<?>> getTypeHandlerMap() {
    return typeHandlerMap;
  }
}
