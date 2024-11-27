package tech.wetech.flexmodel.codegen;

import java.util.HashMap;
import java.util.Map;

/**
 * @author cjbi
 */
public class ModelListGenerationContext {

  private ModelListClass modelListClass;
  protected Map<String, Object> variables = new HashMap<>();

  public ModelListClass getModelListClass() {
    return modelListClass;
  }

  public void setModelListClass(ModelListClass modelsClass) {
    this.modelListClass = modelsClass;
  }

   public void putVariable(String key, Object value) {
    variables.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getVariable(String key) {
    return (T) variables.get(key);
  }

}
