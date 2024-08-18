package tech.wetech.flexmodel.codegen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
public class MultipleModelClass {

  private String packageName;
  private final Set<String> imports = new HashSet<>();
  private final List<ModelClass> models = new ArrayList<>();

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public Set<String> getImports() {
    return imports;
  }

  public List<ModelClass> getModels() {
    return models;
  }

}
