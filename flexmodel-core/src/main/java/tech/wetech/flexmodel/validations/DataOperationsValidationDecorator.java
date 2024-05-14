package tech.wetech.flexmodel.validations;

import tech.wetech.flexmodel.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class DataOperationsValidationDecorator extends AbstractDataOperationsDecorator {

  private final String schemaName;
  private final MappedModels mappedModels;
  private final DataValidator dataValidator;

  public DataOperationsValidationDecorator(String schemaName, MappedModels mappedModels, DataOperations dataOperations) {
    super(dataOperations);
    this.schemaName = schemaName;
    this.mappedModels = mappedModels;
    this.dataValidator = new DataValidator(schemaName, mappedModels);
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
    QueryHelper.validate(schemaName, modelName, mappedModels, query);
    return delegate.find(modelName, query, resultType);
  }

  @Override
  public long count(String modelName, Query query) {
    QueryHelper.validate(schemaName, modelName, mappedModels, query);
    return delegate.count(modelName, query);
  }


}
