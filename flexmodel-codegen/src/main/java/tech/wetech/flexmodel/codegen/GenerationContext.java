package tech.wetech.flexmodel.codegen;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class GenerationContext {

  private ModelClass modelClass;
  protected Map<String, Object> variables = new HashMap<>();

  public ModelClass getModelClass() {
    return modelClass;
  }

  public void setModelClass(ModelClass modelClass) {
    this.modelClass = modelClass;
  }

  public void putVariable(String key, Object value) {
    variables.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getVariable(String key) {
    return (T) variables.get(key);
  }

}
