package tech.wetech.flexmodel.codegen;

/**
 * @author cjbi
 */
public class ModelField {

  private ModelClass modelClass;
  private String fieldName;
  private String comment;
  private String typePackage;
  private String shortTypeName;
  private String fullTypeName;
  private boolean identity;
  private boolean nullable;

  public ModelClass getModelClass() {
    return modelClass;
  }

  public void setModelClass(ModelClass modelClass) {
    this.modelClass = modelClass;
  }

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getTypePackage() {
    return typePackage;
  }

  public void setTypePackage(String typePackage) {
    this.typePackage = typePackage;
  }

  public String getShortTypeName() {
    return shortTypeName;
  }

  public void setShortTypeName(String shortTypeName) {
    this.shortTypeName = shortTypeName;
  }

  public String getFullTypeName() {
    return fullTypeName;
  }

  public void setFullTypeName(String fullTypeName) {
    this.fullTypeName = fullTypeName;
  }

  public boolean isIdentity() {
    return identity;
  }

  public void setIdentity(boolean identity) {
    this.identity = identity;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable(boolean nullable) {
    this.nullable = nullable;
  }

}
