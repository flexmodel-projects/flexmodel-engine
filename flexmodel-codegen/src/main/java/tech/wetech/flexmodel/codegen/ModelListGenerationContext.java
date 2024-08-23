package tech.wetech.flexmodel.codegen;

/**
 * @author cjbi
 */
public class ModelListGenerationContext {

  private String schemaName;
  private String packageName;
  private ModelListClass modelListClass;

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public ModelListClass getModelListClass() {
    return modelListClass;
  }

  public void setModelsClass(ModelListClass modelsClass) {
    this.modelListClass = modelsClass;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
}
