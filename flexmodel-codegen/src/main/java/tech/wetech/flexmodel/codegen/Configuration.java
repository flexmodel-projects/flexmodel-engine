package tech.wetech.flexmodel.codegen;

/**
 * 配置类，用于存储和处理代码生成的各种设置，包括数据库连接信息和生成策略。
 *
 * @author cjbi
 */
public class Configuration {

  private SchemaConfig schema;
  private Target target;

  public SchemaConfig getSchema() {
    return schema;
  }

  public void setSchema(SchemaConfig schema) {
    this.schema = schema;
  }

  public Target getTarget() {
    return target;
  }

  public void setTarget(Target target) {
    this.target = target;
  }

  public static class SchemaConfig {
    private String schemaName;
    private String importScript;
    private String includes;
    private String excludes;
    private DSConfig dsConfig;

    public String getSchemaName() {
      return schemaName;
    }

    public void setSchemaName(String schemaName) {
      this.schemaName = schemaName;
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

    public DSConfig getDsConfig() {
      return dsConfig;
    }

    public void setDsConfig(DSConfig dsConfig) {
      this.dsConfig = dsConfig;
    }
  }

  /**
   * 数据库配置
   */
  public static class DSConfig {

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

  public static class Target {

    private String packageName = "";
    private String director = "scr/main/java";

    public String getPackageName() {
      return packageName;
    }

    public void setPackageName(String packageName) {
      this.packageName = packageName;
    }

    public String getDirector() {
      return director;
    }

    public void setDirector(String director) {
      this.director = director;
    }
  }

}
