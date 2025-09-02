package tech.wetech.flexmodel.codegen;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.ModelDefinition;
import tech.wetech.flexmodel.model.field.EnumRefField;
import tech.wetech.flexmodel.model.field.RelationField;
import tech.wetech.flexmodel.model.field.TypedField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author cjbi
 */
public class ModelClass extends AbstractClass<ModelClass> {

  @JsonManagedReference
  private ModelField idField;
  private final List<ModelField> basicFields = new ArrayList<>();
  private final List<ModelField> enumFields = new ArrayList<>();
  private final List<ModelField> relationFields = new ArrayList<>();
  private final List<ModelField> allFields = new ArrayList<>();
  private ModelDefinition original;

  public ModelClass() {
    super();
    // default imports
    getImports().add("tech.wetech.flexmodel.annotation.*");
  }

  public static ModelClass buildModelClass(String packageName, String schemaName, EntityDefinition entity) {
    return buildModelClass(null, packageName, schemaName, entity);
  }

  public static ModelClass buildModelClass(String replaceString, String packageName, String schemaName, EntityDefinition entity) {
    String cCamelName = StringUtils.snakeToCamel(replaceString != null ? entity.getName().replaceAll(replaceString, "") : entity.getName());
    ModelClass modelClass = new ModelClass()
      .setComment(entity.getComment())
      .setVariableName(StringUtils.uncapitalize(cCamelName))
      .setShortClassName(StringUtils.capitalize(cCamelName))
      .setPackageName(packageName + ".entity")
      .setSchemaName(schemaName)
      .setName(entity.getName())
      .setFullClassName(packageName + ".entity" + "." + StringUtils.capitalize(cCamelName))
      .setOriginal(entity);

    for (TypedField<?, ?> field : entity.getFields()) {

      ModelField modelField = new ModelField()
        .setModelClass(modelClass)
        .setIdentity(field.isIdentity())
        .setVariableName(StringUtils.snakeToCamel(field.getName()))
        .setName(field.getName())
        .setOriginal(field)
        .setComment(field.getComment());

      switch (field) {
        case RelationField relationField -> {
          String ftName = StringUtils.capitalize(
            StringUtils.snakeToCamel(
              replaceString != null ?
                relationField.getFrom().replaceAll(replaceString, "") :
                relationField.getFrom())
          );
          // relation field
          if (relationField.isMultiple()) {
            modelField.setTypePackage("java.util")
              .setFullTypeName("java.util.List")
              .setShortTypeName("List<" + ftName + ">")
              .setRelationField(true);
          } else {
            modelField.setTypePackage(null)
              .setFullTypeName(modelField.getTypePackage() + "." + ftName)
              .setShortTypeName(ftName)
              .setRelationField(true);
            modelClass.getRelationFields().add(modelField);
          }

        }
        case EnumRefField anEnumField -> {
          String ftName = StringUtils.capitalize(
            StringUtils.snakeToCamel(
              replaceString != null ?
                anEnumField.getFrom().replaceAll(replaceString, "") :
                anEnumField.getFrom())
          );
          if (anEnumField.isMultiple()) {
            modelField.setTypePackage("java.util")
              .setFullTypeName("java.util.Set")
              .setShortTypeName("Set<" + ftName + ">")
              .setEnumField(true);
          } else {
            modelField.setTypePackage(packageName + ".enumeration")
              .setFullTypeName(modelField.getTypePackage() + "." + ftName)
              .setShortTypeName(ftName)
              .setEnumField(true);
          }
          modelClass.getEnumFields().add(modelField);
          modelClass.getImports().add(packageName + ".enumeration." + ftName);
        }
        default -> {
          // basic field
          JavaType typeInfo = JavaType.getTypeInfo(field.getType());
          modelField.setTypePackage(typeInfo.getTypePackage())
            .setShortTypeName(typeInfo.getShortTypeName())
            .setFullTypeName(typeInfo.getFullTypeName())
            .setBasicField(true);

          modelClass.getBasicFields().add(modelField);
        }
      }

      if (modelField.isIdentity()) {
        modelField.setIdentity(true);
        modelClass.setIdField(modelField);
      }

      if (modelField.getTypePackage() != null) {
        modelClass.getImports().add(modelField.getFullTypeName());
      }
      modelClass.getAllFields().add(modelField);
    }
    return modelClass;
  }

  public ModelDefinition getOriginal() {
    return original;
  }

  public ModelClass setOriginal(ModelDefinition original) {
    this.original = original;
    return this;
  }

  public ModelField getIdField() {
    return idField;
  }

  public ModelClass setIdField(ModelField idField) {
    this.idField = idField;
    return this;
  }

  public List<ModelField> getBasicFields() {
    return basicFields;
  }

  public List<ModelField> getEnumFields() {
    return enumFields;
  }

  public List<ModelField> getRelationFields() {
    return relationFields;
  }

  public List<ModelField> getAllFields() {
    return allFields;
  }

  public ModelField getField(String name) {
    return allFields.stream()
      .filter(f -> f.getVariableName().equals(name))
      .findFirst()
      .orElse(null);
  }

}
