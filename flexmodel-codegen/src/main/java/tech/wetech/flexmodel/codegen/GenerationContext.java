package tech.wetech.flexmodel.codegen;

import java.util.*;

/**
 * @author cjbi
 */
public class GenerationContext {
  private String packageName;
  private String schemaName;
  private ModelClass modelClass;
  private EnumClass enumClass;
  private final Set<String> imports = new HashSet<>();
  private final List<ModelClass> modelClassList = new ArrayList<>();
  private final List<EnumClass> enumClassList = new ArrayList<>();
  protected Map<String, Object> variables = new HashMap<>();
  private int modelIndex = 0;
  private int enumIndex = 0;

  public boolean nextModel() {
    if (modelClassList.size() > modelIndex) {
      modelClass = modelClassList.get(modelIndex++);
      return true;
    } else {
      modelIndex = 0;
      modelClass = null;
      return false;
    }
  }

  public boolean nextEnum() {
    if (enumClassList.size() > enumIndex) {
      enumClass = enumClassList.get(enumIndex++);
      return true;
    } else {
      enumIndex = 0;
      enumClass = null;
      return false;
    }
  }

  public GenerationContext setModelClass(ModelClass modelClass) {
    this.modelClass = modelClass;
    return this;
  }

  public GenerationContext setEnumClass(EnumClass enumClass) {
    this.enumClass = enumClass;
    return this;
  }

  public String getPackageNameAsPath() {
    return packageName.replaceAll("\\.", "/");
  }

  public ModelClass getModelClass() {
    return modelClass;
  }

  public void cleanIte() {
    modelClass = null;
    enumClass = null;
  }


  public EnumClass getEnumClass() {
    return enumClass;
  }

  public void putVariable(String key, Object value) {
    variables.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getVariable(String key) {
    return (T) variables.get(key);
  }

  public String getPackageName() {
    return packageName;
  }

  public GenerationContext setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public GenerationContext setSchemaName(String schemaName) {
    this.schemaName = schemaName;
    return this;
  }

  public Set<String> getImports() {
    return imports;
  }

  public List<ModelClass> getModelClassList() {
    return modelClassList;
  }

  public List<EnumClass> getEnumClassList() {
    return enumClassList;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

  public GenerationContext setVariables(Map<String, Object> variables) {
    this.variables = variables;
    return this;
  }

  public boolean containsEnumClass(String name) {
    return enumClassList.stream()
      .anyMatch(e -> e.getOriginalEnum().getName().equals(name));
  }

  public boolean containsModelClass(String name) {
    return modelClassList.stream()
      .anyMatch(e -> e.getOriginalModel().getName().equals(name));
  }

}
