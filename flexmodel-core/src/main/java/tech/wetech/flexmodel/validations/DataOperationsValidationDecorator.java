package tech.wetech.flexmodel.validations;

import tech.wetech.flexmodel.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class DataOperationsValidationDecorator extends AbstractDataOperationsDecorator {
  private final DataValidator dataValidator;

  public DataOperationsValidationDecorator(AbstractSessionContext sessionContext, DataOperations dataOperations) {
    super(sessionContext, dataOperations);
    this.dataValidator = new DataValidator(sessionContext.getSchemaName(), sessionContext.getMappedModels());
  }

  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {
    dataValidator.validateAll(modelName, record);
    return delegate.insert(modelName, record, id);
  }

  public int updateById(String modelName, Map<String, Object> record, Object id) {
    dataValidator.validate(modelName, record);
    return delegate.updateById(modelName, record, id);
  }

  public int update(String modelName, Map<String, Object> record, String filter) {
    dataValidator.validate(modelName, record);
    return delegate.update(modelName, record, filter);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    QueryHelper.validate(sessionContext.getSchemaName(), modelName, sessionContext.getMappedModels(), query);
    return delegate.find(modelName, query, resultType);
  }

  @Override
  public long count(String modelName, Query query) {
    QueryHelper.validate(sessionContext.getSchemaName(), modelName, sessionContext.getMappedModels(), query);
    return delegate.count(modelName, query);
  }


}
