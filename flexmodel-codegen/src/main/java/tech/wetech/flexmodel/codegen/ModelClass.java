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


  public String getPackageName() {
    return packageName;
  }

  public ModelClass setPackageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  public Set<String> getImports() {
    return imports;
  }

  public String getVariableName() {
    return variableName;
  }

  public ModelClass setVariableName(String variableName) {
    this.variableName = variableName;
    return this;
  }

  public String getLowerCaseName() {
    return lowerCaseName;
  }

  public ModelClass setLowerCaseName(String lowerCaseName) {
    this.lowerCaseName = lowerCaseName;
    return this;
  }

  public String getShortClassName() {
    return shortClassName;
  }

  public ModelClass setShortClassName(String shortClassName) {
    this.shortClassName = shortClassName;
    return this;
  }

  public String getFullClassName() {
    return fullClassName;
  }

  public ModelClass setFullClassName(String fullClassName) {
    this.fullClassName = fullClassName;
    return this;
  }

  public String getComment() {
    return comment;
  }

  public ModelClass setComment(String comment) {
    this.comment = comment;
    return this;
  }

  public ModelField getIdField() {
    return idField;
  }

  public ModelClass setIdField(ModelField idField) {
    this.idField = idField;
    return this;
  }

  public List<ModelField> getAllFields() {
    return allFields;
  }

  public Model getOriginalModel() {
    return originalModel;
  }

  public ModelClass setOriginalModel(Model originalModel) {
    this.originalModel = originalModel;
    return this;
  }
}
