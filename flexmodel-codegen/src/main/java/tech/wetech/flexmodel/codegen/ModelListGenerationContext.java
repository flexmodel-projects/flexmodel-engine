package tech.wetech.flexmodel.codegen;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class ModelListGenerationContext {

  private ModelListClass modelListClass;
  protected Map<String, Object> extendVariables = new HashMap<>();

  public ModelListClass getModelListClass() {
    return modelListClass;
  }

  public void setModelListClass(ModelListClass modelsClass) {
    this.modelListClass = modelsClass;
  }

   public void putExtendVariable(String key, Object value) {
    extendVariables.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getExtendVariable(String key) {
    return (T) extendVariables.get(key);
  }

}
