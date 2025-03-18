package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.ScalarType.*;

/**
 * 用于执行代码生成，允许通过命令行参数配置。
 *
 * @author cjbi
 */
public class GenerationTool {

  private static final Map<String, TypeInfo> TYPE_MAPPING;

  record TypeInfo(String typePackage, String shortTypeName, String fullTypeName) {
  }

  static {
    TYPE_MAPPING = new HashMap<>();
    TYPE_MAPPING.put(ID.getType(), null);
    TYPE_MAPPING.put(RELATION.getType(), null);
    TYPE_MAPPING.put(STRING.getType(), new TypeInfo(null, "String", "String"));
    TYPE_MAPPING.put(TEXT.getType(), new TypeInfo(null, "String", "String"));
    TYPE_MAPPING.put(DECIMAL.getType(), new TypeInfo(null, "Double", "Double"));
    TYPE_MAPPING.put(INT.getType(), new TypeInfo(null, "Integer", "Integer"));
    TYPE_MAPPING.put(BIGINT.getType(), new TypeInfo(null, "Long", "Long"));
    TYPE_MAPPING.put(BOOLEAN.getType(), new TypeInfo(null, "Boolean", "Boolean"));
    TYPE_MAPPING.put(DATETIME.getType(), new TypeInfo("java.time", "LocalDateTime", "java.time.LocalDateTime"));
    TYPE_MAPPING.put(DATE.getType(), new TypeInfo("java.time", "LocalDate", "java.time.LocalDate"));
    TYPE_MAPPING.put(JSON.getType(), new TypeInfo(null, "Object", "Object"));
  }

  public static void run(Configuration configuration) {
    Configuration.Schema schema = configuration.getSchema();
    JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();
    List<TypeWrapper> models = new ArrayList<>();
    List<ImportDescribe.ImportData> data = new ArrayList<>();
    // read from script
    String importScript = configuration.getSchema().getImportScript();
    File scriptFile = Path.of(configuration.getTarget().getBaseDir(), importScript).toFile();
    System.out.println("Import Script File Path: " + scriptFile.getAbsolutePath());
    if (scriptFile.exists()) {
      System.out.println("Script file is exists, import Script File: " + scriptFile);
      try {
        String content = Files.readString(scriptFile.toPath());
        ImportDescribe describe = jsonObjectConverter.parseToObject(content, ImportDescribe.class);
        Map<String, Object> map = jsonObjectConverter.parseToMap(content);
        models.addAll(describe.getSchema());
        data.addAll(describe.getData());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    String packageName = configuration.getTarget().getPackageName();
    String targetDirectory = configuration.getTarget().getDirectory() + File.separator +
                             packageName.replace(".", File.separator);
    StringUtils.createDirectoriesIfNotExists(targetDirectory + File.separator + "dao");
    StringUtils.createDirectoriesIfNotExists(targetDirectory + File.separator + "entity");
    StringUtils.createDirectoriesIfNotExists(targetDirectory + File.separator + "enumeration");
    StringUtils.createDirectoriesIfNotExists(targetDirectory + File.separator + "dsl");

    Map<String, ModelClass> modelClassMap = new HashMap<>();
    Map<String, EnumClass> enumClassMap = new HashMap<>();
    for (TypeWrapper model : models) {
      if (model instanceof Entity) {
        modelClassMap.put(model.getName(), buildModelClass(configuration.getTarget().getReplaceString(), packageName, schema.getName(), (Entity) model));
      } else if (model instanceof Enum) {
        enumClassMap.put(model.getName(), buildEnumClass(packageName, schema.getName(), (Enum) model));
      }
    }

    // generate single model file
    DaoGenerator daoGenerator = new DaoGenerator();
    PojoGenerator pojoGenerator = new PojoGenerator();
    DSLGenerator dslGenerator = new DSLGenerator();
    for (TypeWrapper model : models) {
      if (model instanceof Entity) {
        GenerationContext context = new GenerationContext();
        ModelClass modelClass = modelClassMap.get(model.getName());
        context.setModelClass(modelClass);
        context.putVariable("rootPackage", packageName);
        pojoGenerator.generate(context, Path.of(targetDirectory, "entity", modelClass.getShortClassName() + ".java").toString());
        dslGenerator.generate(context, Path.of(targetDirectory, "dsl", modelClass.getShortClassName() + "DSL.java").toString());
        daoGenerator.generate(context, Path.of(targetDirectory, "dao", modelClass.getShortClassName() + "DAO.java").toString());
      }
    }

    // generate multiple model file
    ModelListGenerationContext multipleModelGenerationContext = new ModelListGenerationContext();
    multipleModelGenerationContext.putVariable("rootPackage", packageName);
    ModelListClass multipleModelClass = new ModelListClass();
    multipleModelClass.setSchemaName(schema.getName());
    multipleModelClass.setPackageName(packageName);
    multipleModelGenerationContext.setModelListClass(multipleModelClass);
    for (TypeWrapper model : models) {
      if (model instanceof Entity) {
        ModelClass modelClass = modelClassMap.get(model.getName());
        multipleModelClass.getModelList().add(modelClass);
        multipleModelClass.getImports().add(modelClass.getFullClassName());
      } else if (model instanceof Enum) {
        EnumClass enumClass = enumClassMap.get(model.getName());
        multipleModelClass.getEnumList().add(enumClass);
      }
    }

    // generate enum class file
    EnumGenerator enumGenerator = new EnumGenerator();
    enumClassMap.forEach((name, enumClass) -> {
        GenerationContext context = new GenerationContext();
        context.setEnumClass(enumClass);
        enumGenerator.generate(context, Path.of(targetDirectory, "enumeration", enumClass.getShortClassName() + ".java").toString());
      }
    );

    SchemaGenerator schemaClassGenerator = new SchemaGenerator();
    multipleModelGenerationContext.putVariable("import_data", data);
    schemaClassGenerator.generate(multipleModelGenerationContext, Path.of(targetDirectory, StringUtils.capitalize(schema.getName()) + ".java").toString());

    StringUtils.createDirectoriesIfNotExists(configuration.getTarget().getBaseDir() + "/target/classes/META-INF/services");
    BuildItemSPIFileGenerator buildItemSPIFileGenerator = new BuildItemSPIFileGenerator();
    buildItemSPIFileGenerator.generate(multipleModelGenerationContext, Path.of(
      configuration.getTarget().getBaseDir(),
      "target/classes/META-INF/services",
      "tech.wetech.flexmodel.BuildItem"
    ).toString());
  }

  public static EnumClass buildEnumClass(String packageName, String schemaName, Enum anEnum) {
    String ftName = StringUtils.capitalize(StringUtils.snakeToCamel(anEnum.getName()));
    EnumClass enumClass = new EnumClass();
    enumClass.setSchemaName(schemaName);
    enumClass.setPackageName(packageName + ".enumeration");
    enumClass.setShortClassName(ftName);
    enumClass.setFullClassName(enumClass.getPackageName() + "." + ftName);
    enumClass.setVariableName(StringUtils.uncapitalize(ftName));
    enumClass.setElements(anEnum.getElements());
    enumClass.setComment(anEnum.getComment());
    enumClass.setOriginalEnum(anEnum);
    return enumClass;
  }

  public static ModelClass buildModelClass(String packageName, String schemaName, Entity entity) {
    return buildModelClass(null, packageName, schemaName, entity);
  }

  public static ModelClass buildModelClass(String replaceString, String packageName, String schemaName, Entity entity) {
    String cCamelName = StringUtils.snakeToCamel(replaceString != null ? entity.getName().replaceAll(replaceString, "") : entity.getName());
    ModelClass modelClass = new ModelClass()
      .setComment(entity.getComment())
      .setVariableName(StringUtils.uncapitalize(cCamelName))
      .setLowerCaseName(StringUtils.uncapitalize(cCamelName))
      .setShortClassName(StringUtils.capitalize(cCamelName))
      .setPackageName(packageName + ".entity")
      .setSchemaName(schemaName)
      .setModelName(entity.getName())
      .setFullClassName(packageName + ".entity" + "." + StringUtils.capitalize(cCamelName))
      .setOriginalModel(entity);

    for (TypedField<?, ?> field : entity.getFields()) {

      ModelField modelField = new ModelField()
        .setModelClass(modelClass)
        .setVariableName(StringUtils.snakeToCamel(field.getName()))
        .setFieldName(field.getName())
        .setOriginalField(field)
        .setComment(field.getComment());

      switch (field) {
        case IDField idField -> {
          // id field
          TypeInfo typeInfo = TYPE_MAPPING.get(idField.getGeneratedValue().getType());

          modelField.setTypePackage(typeInfo.typePackage())
            .setShortTypeName(typeInfo.shortTypeName())
            .setFullTypeName(typeInfo.fullTypeName())
            .setIdentity(true);

          modelClass.setIdField(modelField);

        }
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
        case EnumField anEnumField -> {
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
          TypeInfo typeInfo = TYPE_MAPPING.get(field.getType());
          modelField.setTypePackage(typeInfo.typePackage())
            .setShortTypeName(typeInfo.shortTypeName())
            .setFullTypeName(typeInfo.fullTypeName())
            .setBasicField(true);

          modelClass.getBasicFields().add(modelField);
        }
      }

      if (modelField.getTypePackage() != null) {
        modelClass.getImports().add(modelField.getFullTypeName());
      }
      modelClass.getAllFields().add(modelField);
    }
    return modelClass;
  }

}
