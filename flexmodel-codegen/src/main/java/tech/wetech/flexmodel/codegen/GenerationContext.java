package tech.wetech.flexmodel.codegen;

import groovy.lang.MissingPropertyException;
import tech.wetech.flexmodel.ModelImportBundle;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.EnumDefinition;
import tech.wetech.flexmodel.model.SchemaObject;

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
  private int eachIndex = 0;

  public boolean nextModel() {
    if (modelClassList.size() > eachIndex) {
      modelClass = modelClassList.get(eachIndex++);
      return true;
    } else {
      eachIndex = 0;
      modelClass = null;
      return false;
    }
  }

  public boolean nextEnum() {
    if (enumClassList.size() > eachIndex) {
      enumClass = enumClassList.get(eachIndex++);
      return true;
    } else {
      eachIndex = 0;
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

  /**
   * Enable dynamic property access from Groovy scripts.
   * Example: after calling putVariable("foo", "bar"), Groovy can use context.foo
   */
  public Object propertyMissing(String name) {
    if (variables.containsKey(name)) {
      return variables.get(name);
    }
    throw new MissingPropertyException(name, this.getClass());
  }

  /**
   * Allow setting variables via dynamic property assignment in Groovy: context.foo = "bar"
   */
  public void propertyMissing(String name, Object value) {
    variables.put(name, value);
  }

  public boolean containsEnumClass(String name) {
    return enumClassList.stream()
      .anyMatch(e -> e.getOriginal().getName().equals(name));
  }

  public boolean containsModelClass(String name) {
    return modelClassList.stream()
      .anyMatch(e -> e.getOriginal().getName().equals(name));
  }

  public static GenerationContext buildGenerationContext(Configuration configuration, ModelImportBundle importDescribe) {
    SchemaConfig schema = configuration.getSchemas().stream()
      .filter(s -> s.getName().equals(importDescribe.getSchemaName()))
      .findFirst()
      .orElseThrow();
    List<SchemaObject> models = importDescribe.getObjects();
    List<ModelImportBundle.ImportData> data = importDescribe.getData();
    String packageName = schema.getPackageName();
    Map<String, ModelClass> modelClassMap = new HashMap<>();
    Map<String, EnumClass> enumClassMap = new HashMap<>();
    for (SchemaObject model : models) {
      if (model instanceof EntityDefinition) {
        modelClassMap.put(model.getName(), ModelClass.buildModelClass(schema.getReplaceString(), packageName, schema.getName(), (EntityDefinition) model));
      } else if (model instanceof EnumDefinition) {
        enumClassMap.put(model.getName(), EnumClass.buildEnumClass(packageName, schema.getName(), (EnumDefinition) model));
      }
    }

    GenerationContext context = new GenerationContext();
    context.setSchemaName(schema.getName());
    context.setPackageName(packageName);
    for (SchemaObject model : models) {
      if (model instanceof EntityDefinition) {
        ModelClass modelClass = modelClassMap.get(model.getName());
        context.getModelClassList().add(modelClass);
        context.getImports().add(modelClass.getFullClassName());
      } else if (model instanceof EnumDefinition) {
        EnumClass enumClass = enumClassMap.get(model.getName());
        context.getEnumClassList().add(enumClass);
        context.getImports().add(enumClass.getFullClassName());
      }
    }
    context.putVariable("rootPackage", packageName);
    context.putVariable("import_data", data);
    return context;
  }

}
