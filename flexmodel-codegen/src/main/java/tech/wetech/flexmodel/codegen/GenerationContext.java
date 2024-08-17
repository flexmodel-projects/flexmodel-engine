package tech.wetech.flexmodel.codegen;

/**
 * @author cjbi
 */
public class GenerationContext {

  private String schemaName;
  private String packageName;
  private ModelClass modelClass;
  private String targetDirectory;

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public ModelClass getModelClass() {
    return modelClass;
  }

  public void setModelClass(ModelClass modelClass) {
    this.modelClass = modelClass;
  }

  public String getTargetDirectory() {
    return targetDirectory;
  }

  public void setTargetDirectory(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }
}
