package tech.wetech.flexmodel.validations;

import tech.wetech.flexmodel.AbstractDataOperationsDecorator;
import tech.wetech.flexmodel.DataOperations;
import tech.wetech.flexmodel.MappedModels;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class DataOperationsValidationDecorator extends AbstractDataOperationsDecorator {

  private final DataValidator dataValidator;

  public DataOperationsValidationDecorator(String schemaName, MappedModels mappedModels, DataOperations dataOperations) {
    super(dataOperations);
    this.dataValidator = new DataValidator(schemaName, mappedModels);
  }

  public int insert(String modelName, Map<String, Object> record) {
    dataValidator.validateAll(modelName, record);
    return delegate.insert(modelName, record);
  }

  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {
    dataValidator.validateAll(modelName, record);
    return delegate.insert(modelName, record, id);
  }

  @Override
  public int insertAll(String modelName, List<Map<String, Object>> records) {
    for (Map<String, Object> record : records) {
      dataValidator.validateAll(modelName, record);
    }
    return delegate.insertAll(modelName, records);
  }

  public int updateById(String modelName, Map<String, Object> record, Object id) {
    dataValidator.validate(modelName, record);
    return delegate.updateById(modelName, record, id);
  }

  public int update(String modelName, Map<String, Object> record, String filter) {
    dataValidator.validate(modelName, record);
    return delegate.update(modelName, record, filter);
  }


}
