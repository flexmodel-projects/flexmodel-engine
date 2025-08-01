package tech.wetech.flexmodel.core.mongodb;

import com.mongodb.client.MongoDatabase;
import tech.wetech.flexmodel.core.ExpressionCalculator;
import tech.wetech.flexmodel.core.JsonObjectConverter;
import tech.wetech.flexmodel.core.MappedModels;
import tech.wetech.flexmodel.core.model.field.ScalarType;
import tech.wetech.flexmodel.core.session.AbstractSessionContext;
import tech.wetech.flexmodel.core.session.SessionFactory;
import tech.wetech.flexmodel.core.type.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class MongoContext extends AbstractSessionContext {

  private final MongoDatabase mongoDatabase;
  private ExpressionCalculator<String> conditionCalculator;
  private final Map<String, TypeHandler<?>> typeHandlerMap = new HashMap<>();

  public MongoContext(String schemaName, MongoDatabase mongoDatabase, MappedModels mappedModels, JsonObjectConverter jsonObjectConverter, SessionFactory factory) {
    super(schemaName, mappedModels, jsonObjectConverter, factory);
    this.mongoDatabase = mongoDatabase;
    this.conditionCalculator = new DefaultMongoExpressionCalculator();

    this.typeHandlerMap.put(ScalarType.STRING.getType(), new StringTypeHandler());
    this.typeHandlerMap.put(ScalarType.FLOAT.getType(), new DecimalTypeHandler());
    this.typeHandlerMap.put(ScalarType.INT.getType(), new IntTypeHandler());
    this.typeHandlerMap.put(ScalarType.LONG.getType(), new BigintTypeHandler());
    this.typeHandlerMap.put(ScalarType.BOOLEAN.getType(), new BooleanTypeHandler());
    this.typeHandlerMap.put(ScalarType.DATETIME.getType(), new DateTimeTypeHandler());
    this.typeHandlerMap.put(ScalarType.DATE.getType(), new DateTypeHandler());
    this.typeHandlerMap.put(ScalarType.TIME.getType(), new TimeTypeHandler());
    this.typeHandlerMap.put(ScalarType.JSON.getType(), new JsonTypeHandler(jsonObjectConverter));
    this.typeHandlerMap.put(ScalarType.ENUM.getType(), new EnumTypeHandler());
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
