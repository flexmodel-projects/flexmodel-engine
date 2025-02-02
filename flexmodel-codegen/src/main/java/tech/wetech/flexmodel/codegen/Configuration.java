package tech.wetech.flexmodel.codegen;

import java.io.Serializable;

/**
 * 配置类，用于存储和处理代码生成的各种设置，包括数据库连接信息和生成策略。
 *
 * @author cjbi
 */
public class Configuration implements Serializable {

  private Schema schema;
  private Target target;

  public Schema getSchema() {
    return schema;
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  public Target getTarget() {
    return target;
  }

  public void setTarget(Target target) {
    this.target = target;
  }

  public static class Schema implements Serializable {
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

  public static class Target implements Serializable {

    private String packageName = "tech.wetech";
    private String directory =  null;
    private String baseDir = null;

    public String getPackageName() {
      return packageName;
    }

    public void setPackageName(String packageName) {
      this.packageName = packageName;
    }

    public String getDirectory() {
      return directory;
    }

    public void setDirectory(String directory) {
      this.directory = directory;
    }

    public String getBaseDir() {
      return baseDir;
    }

    public void setBaseDir(String baseDir) {
      this.baseDir = baseDir;
    }
  }

}
