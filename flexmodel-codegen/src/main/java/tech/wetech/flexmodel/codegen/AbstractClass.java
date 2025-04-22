package tech.wetech.flexmodel.codegen;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cjbi
 */
public abstract class AbstractClass<SELF extends AbstractClass<SELF>> implements Serializable {

  private final Set<String> imports = new HashSet<>();
  private String name;
  private String schemaName;
  private String packageName;
  private String shortClassName;
  private String fullClassName;
  private String variableName;
  private String comment;

  public Set<String> getImports() {
    return imports;
  }

  public String getSchemaName() {
    return schemaName;
  }

  public SELF setSchemaName(String schemaName) {
    this.schemaName = schemaName;
    return self();
  }

  public String getPackageName() {
    return packageName;
  }

  public SELF setPackageName(String packageName) {
    this.packageName = packageName;
    return self();
  }

  public String getShortClassName() {
    return shortClassName;
  }

  public SELF setShortClassName(String shortClassName) {
    this.shortClassName = shortClassName;
    return self();
  }

  public String getFullClassName() {
    return fullClassName;
  }

  public SELF setFullClassName(String fullClassName) {
    this.fullClassName = fullClassName;
    return self();
  }

  public String getVariableName() {
    return variableName;
  }

  public SELF setVariableName(String variableName) {
    this.variableName = variableName;
    return self();
  }

  public String getComment() {
    return comment;
  }

  public SELF setComment(String comment) {
    this.comment = comment;
    return self();
  }

  public String getName() {
    return name;
  }

  public SELF setName(String name) {
    this.name = name;
    return self();
  }

  @SuppressWarnings("unchecked")
  private SELF self() {
    return (SELF) this;
  }

}
