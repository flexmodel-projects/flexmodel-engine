package tech.wetech.flexmodel.codegen;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class GenerationContext {

  private ModelClass modelClass;
  protected Map<String, Object> extendVariables = new HashMap<>();

  public ModelClass getModelClass() {
    return modelClass;
  }

  public void setModelClass(ModelClass modelClass) {
    this.modelClass = modelClass;
  }

  public void putExtendVariable(String key, Object value) {
    extendVariables.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getExtendVariable(String key) {
    return (T) extendVariables.get(key);
  }

}
