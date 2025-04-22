package tech.wetech.flexmodel.codegen;

import java.io.Serializable;

/**
 * @author cjbi
 */
public class Schema implements Serializable {
  private String name;
  private String importScript = "import.json";

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getImportScript() {
    return importScript;
  }

  public Schema setImportScript(String importScript) {
    this.importScript = importScript;
    return this;
  }

}
