package tech.wetech.flexmodel.codegen;

/**
 * @author cjbi
 */
public class MultipleModelGenerationContext {

  private String schemaName;
  private String packageName;
  private MultipleModelClass modelsClass;
  private String targetDirectory;

  public String getSchemaName() {
    return schemaName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public MultipleModelClass getModelsClass() {
    return modelsClass;
  }

  public void setModelsClass(MultipleModelClass modelsClass) {
    this.modelsClass = modelsClass;
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
