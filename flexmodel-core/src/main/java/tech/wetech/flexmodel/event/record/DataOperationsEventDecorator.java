package tech.wetech.flexmodel.event.record;

import tech.wetech.flexmodel.AbstractDataOperationsDecorator;
import tech.wetech.flexmodel.AbstractSessionContext;
import tech.wetech.flexmodel.DataOperations;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class DataOperationsEventDecorator extends AbstractDataOperationsDecorator {

  public DataOperationsEventDecorator(AbstractSessionContext sessionContext, DataOperations delegate) {
    super(sessionContext, delegate);
  }

  @Override
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> id) {
    return super.insert(modelName, record, id);
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    return super.updateById(modelName, record, id);
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    return super.update(modelName, record, filter);
  }

  @Override
  public int deleteById(String modelName, Object id) {
    return super.deleteById(modelName, id);
  }

  @Override
  public int delete(String modelName, String filter) {
    return super.delete(modelName, filter);
  }

  @Override
  public int deleteAll(String modelName) {
    return super.deleteAll(modelName);
  }

}
