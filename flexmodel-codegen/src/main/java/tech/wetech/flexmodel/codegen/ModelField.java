package tech.wetech.flexmodel.codegen;

import com.fasterxml.jackson.annotation.JsonBackReference;
import tech.wetech.flexmodel.Field;

/**
 * @author cjbi
 */
public class ModelField {

  @JsonBackReference
  private ModelClass modelClass;
  private String variableName;
  private String fieldName;
  private String comment;
  private String typePackage;
  private String shortTypeName;
  private String fullTypeName;
  private boolean identity;
  private boolean basicField;
  private boolean relationField;
  private boolean enumField;
  private Field originalField;

  public ModelClass getModelClass() {
    return modelClass;
  }

  public ModelField setModelClass(ModelClass modelClass) {
    this.modelClass = modelClass;
    return this;
  }

  public String getVariableName() {
    return variableName;
  }

  public ModelField setVariableName(String fieldName) {
    this.variableName = fieldName;
    return this;
  }

  public String getFieldName() {
    return fieldName;
  }

  public ModelField setFieldName(String fieldName) {
    this.fieldName = fieldName;
    return this;
  }

  public String getComment() {
    return comment;
  }

  public ModelField setComment(String comment) {
    this.comment = comment;
    return this;
  }

  public String getTypePackage() {
    return typePackage;
  }

  public ModelField setTypePackage(String typePackage) {
    this.typePackage = typePackage;
    return this;
  }

  public String getShortTypeName() {
    return shortTypeName;
  }

  public ModelField setShortTypeName(String shortTypeName) {
    this.shortTypeName = shortTypeName;
    return this;
  }

  public String getFullTypeName() {
    return fullTypeName;
  }

  public ModelField setFullTypeName(String fullTypeName) {
    this.fullTypeName = fullTypeName;
    return this;
  }

  public boolean isIdentity() {
    return identity;
  }

  public ModelField setIdentity(boolean identity) {
    this.identity = identity;
    return this;
  }

  public boolean isBasicField() {
    return basicField;
  }

  public ModelField setBasicField(boolean basicField) {
    this.basicField = basicField;
    return this;
  }

  public boolean isRelationField() {
    return relationField;
  }

  public boolean isEnumField() {
    return enumField;
  }

  public ModelField setEnumField(boolean enumField) {
    this.enumField = enumField;
    return this;
  }

  public ModelField setRelationField(boolean relationField) {
    this.relationField = relationField;
    return this;
  }

  public Field getOriginalField() {
    return originalField;
  }

  public ModelField setOriginalField(Field originalField) {
    this.originalField = originalField;
    return this;
  }
}
