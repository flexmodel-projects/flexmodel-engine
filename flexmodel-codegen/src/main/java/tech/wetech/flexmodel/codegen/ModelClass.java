package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author cjbi
 */
public class ModelClass implements Serializable {

  private String packageName;
  private final Set<String> imports = new HashSet<>();
  private String variableName;
  private String lowerCaseName;
  private String shortClassName;
  private String fullClassName;
  private String comment;
  private ModelField idField;
  private final List<ModelField> allFields = new ArrayList<>();
  private Model originalModel;

  public String getVariableName() {
    return variableName;
  }

  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  public String getLowerCaseName() {
    return lowerCaseName;
  }

  public void setLowerCaseName(String lowerCaseName) {
    this.lowerCaseName = lowerCaseName;
  }

  public String getShortClassName() {
    return shortClassName;
  }

  public void setShortClassName(String shortClassName) {
    this.shortClassName = shortClassName;
  }

  public String getFullClassName() {
    return fullClassName;
  }

  public void setFullClassName(String fullClassName) {
    this.fullClassName = fullClassName;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public ModelField getIdField() {
    return idField;
  }

  public void setIdField(ModelField idField) {
    this.idField = idField;
  }

  public List<ModelField> getAllFields() {
    return allFields;
  }

  public Set<String> getImports() {
    return imports;
  }

  public Model getOriginalModel() {
    return originalModel;
  }

  public void setOriginalModel(Model originalModel) {
    this.originalModel = originalModel;
  }
}
