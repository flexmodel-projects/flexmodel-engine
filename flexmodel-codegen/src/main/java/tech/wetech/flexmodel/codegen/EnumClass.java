package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.Enum;

import java.io.Serializable;
import java.util.List;

/**
 * @author cjbi
 */
public class EnumClass implements Serializable {

  private String schemaName;
  private String packageName;
  private String shortClassName;
  private List<String> elements;
  private String comment;
  private Enum originalEnum;

  public String getSchemaName() {
    return schemaName;
  }

  public EnumClass setSchemaName(String schemaName) {
    this.schemaName = schemaName;
    return this;
  }

  public String getPackageName() {
    return packageName;
  }

  public EnumClass setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public String getShortClassName() {
    return shortClassName;
  }

  public EnumClass setShortClassName(String shortClassName) {
    this.shortClassName = shortClassName;
    return this;
  }

  public List<String> getElements() {
    return elements;
  }

  public EnumClass setElements(List<String> elements) {
    this.elements = elements;
    return this;
  }

  public String getComment() {
    return comment;
  }

  public EnumClass setComment(String comment) {
    this.comment = comment;
    return this;
  }

  public Enum getOriginalEnum() {
    return originalEnum;
  }

  public EnumClass setOriginalEnum(Enum originalEnum) {
    this.originalEnum = originalEnum;
    return this;
  }
}
