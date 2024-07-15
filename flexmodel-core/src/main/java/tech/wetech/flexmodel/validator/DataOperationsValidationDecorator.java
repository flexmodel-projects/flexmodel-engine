package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.AbstractDataOperationsDecorator;
import tech.wetech.flexmodel.AbstractSessionContext;
import tech.wetech.flexmodel.DataOperations;
import tech.wetech.flexmodel.Query;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class DataOperationsValidationDecorator extends AbstractDataOperationsDecorator {
  private final ValidatorFacade validatorFacade;

  public DataOperationsValidationDecorator(AbstractSessionContext sessionContext, DataOperations dataOperations) {
    super(sessionContext, dataOperations);
    this.validatorFacade = new ValidatorFacade(sessionContext.getSchemaName(), sessionContext.getMappedModels());
  }

  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {
    validatorFacade.validateAll(modelName, record);
    return delegate.insert(modelName, record, id);
  }

  public int updateById(String modelName, Map<String, Object> record, Object id) {
    validatorFacade.validate(modelName, record);
    return delegate.updateById(modelName, record, id);
  }

  public int update(String modelName, Map<String, Object> record, String filter) {
    validatorFacade.validate(modelName, record);
    return delegate.update(modelName, record, filter);
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    return delegate.find(modelName, query, resultType);
  }

  @Override
  public long count(String modelName, Query query) {
    return delegate.count(modelName, query);
  }


}
