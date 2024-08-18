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
    private String importScript;
    private String includes;
    private String excludes;
    private Connect connect;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getImportScript() {
      return importScript;
    }

    public void setImportScript(String importScript) {
      this.importScript = importScript;
    }

    public String getIncludes() {
      return includes;
    }

    public void setIncludes(String includes) {
      this.includes = includes;
    }

    public String getExcludes() {
      return excludes;
    }

    public void setExcludes(String excludes) {
      this.excludes = excludes;
    }

    public Connect getConnect() {
      return connect;
    }

    public void setConnect(Connect connect) {
      this.connect = connect;
    }
  }

  /**
   * 数据库配置
   */
  public static class Connect {

    private String dbKind;
    private String url;
    private String username;
    private String password;

    public String getDbKind() {
      return dbKind;
    }

    public void setDbKind(String dbKind) {
      this.dbKind = dbKind;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }
  }

  public static class Target implements Serializable {

    private String packageName = "com.example";
    private String directory =  null;

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
  }

}
