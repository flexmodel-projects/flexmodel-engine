package tech.wetech.flexmodel.codegen;

import java.io.Serializable;

/**
 * @author cjbi
 */
public class Target implements Serializable {

  private String packageName = "com.example";
  private String replaceString;
  private String directory;
  private String baseDir;

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getReplaceString() {
    return replaceString;
  }

  public void setReplaceString(String replaceString) {
    this.replaceString = replaceString;
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
