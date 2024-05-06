package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.AbstractDataOperationsDecorator;
import tech.wetech.flexmodel.DataOperations;
import tech.wetech.flexmodel.MappedModels;
import tech.wetech.flexmodel.TypeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class DataOperationsCalculationDecorator extends AbstractDataOperationsDecorator {

  private final DataCalculator dataCalculator;

  public DataOperationsCalculationDecorator(String schemaName, MappedModels mappedModels, Map<String, ? extends TypeHandler<?>> typeHandlerMap, DataOperations delegate) {
    super(delegate);
    this.dataCalculator = new DataCalculator(schemaName, mappedModels, typeHandlerMap);
  }

  @Override
  public int insert(String modelName, Map<String, Object> record) {
    return delegate.insert(modelName, dataCalculator.calculateAll(modelName, record));
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {
    return delegate.insert(modelName, dataCalculator.calculateAll(modelName, record), id);
  }

  @Override
  public int insertAll(String modelName, List<Map<String, Object>> records) {
    List<Map<String, Object>> newRecord = new ArrayList<>();
    for (Map<String, Object> record : records) {
      newRecord.add(dataCalculator.calculateAll(modelName, record));
    }
    return delegate.insertAll(modelName, newRecord);
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    return delegate.updateById(modelName, dataCalculator.calculate(modelName, record), id);
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    return delegate.update(modelName, dataCalculator.calculate(modelName, record), filter);
  }

}
