package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.sql.JdbcMappedModels;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static tech.wetech.flexmodel.RelationField.Cardinality.ONE_TO_ONE;

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
    TYPE_MAPPING.put("id", null);
    TYPE_MAPPING.put("relation", null);
    TYPE_MAPPING.put("string", new TypeInfo(null, "String", "String"));
    TYPE_MAPPING.put("text", new TypeInfo(null, "String", "String"));
    TYPE_MAPPING.put("decimal", new TypeInfo(null, "Double", "Double"));
    TYPE_MAPPING.put("int", new TypeInfo(null, "Integer", "Integer"));
    TYPE_MAPPING.put("bigint", new TypeInfo(null, "Long", "Long"));
    TYPE_MAPPING.put("boolean", new TypeInfo(null, "Boolean", "Boolean"));
    TYPE_MAPPING.put("datetime", new TypeInfo("java.time", "LocalDateTime", "java.time.LocalDateTime"));
    TYPE_MAPPING.put("date", new TypeInfo("java.time", "LocalDate", "java.time.LocalDate"));
    TYPE_MAPPING.put("json", new TypeInfo(null, "Object", "Object"));
  }

  public static void run(Configuration configuration) {
    Configuration.Schema schema = configuration.getSchema();
    Configuration.Connect dsConfig = schema.getConnect();
    ConnectionWrapper connectionWrapper = new ConnectionWrapper(dsConfig.getUrl(), dsConfig.getUsername(), dsConfig.getPassword());
    if ("mongodb".equals(dsConfig.getDbKind())) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
    JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();
    JdbcMappedModels mappedModels = new JdbcMappedModels(connectionWrapper, new JacksonObjectConverter());
    Set<Model> models = new HashSet<>(mappedModels.lookup(schema.getName()));

    // read from script
    String importScript = configuration.getSchema().getImportScript();
    File scriptFile = new File(configuration.getTarget().getBaseDir() + "/src/main/resources/" + importScript);
    if (scriptFile.exists()) {
      System.out.println("Import Script File: " + scriptFile);
      try {
        String content = Files.readString(scriptFile.toPath());
        ImportDescribe describe = jsonObjectConverter.parseToObject(content, ImportDescribe.class);
        models.addAll(describe.getSchema());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    PojoGenerator pojoGenerator = new PojoGenerator();
    DaoGenerator daoGenerator = new DaoGenerator();
    String packageName = configuration.getTarget().getPackageName();
    String targetDirectory = configuration.getTarget().getDirectory() + File.separator +
                             packageName.replace(".", File.separator);
    StringUtils.createDirectoriesIfNotExists(targetDirectory + File.separator + "dao");
    StringUtils.createDirectoriesIfNotExists(targetDirectory + File.separator + "entity");

    Map<String, ModelClass> modelClassMap = new HashMap<>();
    for (Model model : models) {
      modelClassMap.put(model.getName(), buildModelClass(packageName, schema.getName(), (Entity) model));
    }

    // generate single model file
    for (Model model : models) {
      GenerationContext context = new GenerationContext();
      context.setSchemaName(schema.getName());
      context.setModelClass(modelClassMap.get(model.getName()));
      context.setPackageName(packageName);
      pojoGenerator.generate(context, Path.of(targetDirectory, "entity", model.getName() + ".java").toString());
      daoGenerator.generate(context, Path.of(targetDirectory, "dao", model.getName() + "DAO.java").toString());
    }
    // generate multiple model file
    ModelListGenerationContext multipleModelGenerationContext = new ModelListGenerationContext();
    multipleModelGenerationContext.setSchemaName(schema.getName());
    multipleModelGenerationContext.setPackageName(packageName);
    ModelListClass multipleModelClass = new ModelListClass();
    multipleModelClass.setPackageName(packageName);
    multipleModelGenerationContext.setModelListClass(multipleModelClass);
    for (Model model : models) {
      ModelClass modelClass = modelClassMap.get(model.getName());
      multipleModelClass.getModelList().add(modelClass);
      multipleModelClass.getImports().add(modelClass.getFullClassName());
    }

    SchemaGenerator schemaClassGenerator = new SchemaGenerator();
    schemaClassGenerator.generate(multipleModelGenerationContext, Path.of(targetDirectory, StringUtils.capitalize(schema.getName()) + ".java").toString());

    StringUtils.createDirectoriesIfNotExists(configuration.getTarget().getBaseDir() + "/target/classes/META-INF/services");
    BuildItemSPIFileGenerator buildItemSPIFileGenerator = new BuildItemSPIFileGenerator();
    buildItemSPIFileGenerator.generate(multipleModelGenerationContext, Path.of(
      configuration.getTarget().getBaseDir(),
      "/target/classes/META-INF/services",
      "tech.wetech.flexmodel.BuildItem"
    ).toString());
  }

  public static ModelClass buildModelClass(String packageName, String schemaName, Entity entity) {

    ModelClass modelClass = new ModelClass()
      .setComment(entity.getComment())
      .setVariableName(StringUtils.uncapitalize(entity.getName()))
      .setLowerCaseName(StringUtils.uncapitalize(entity.getName()))
      .setShortClassName(StringUtils.capitalize(entity.getName()))
      .setPackageName(packageName + ".entity")
      .setSchemaName(schemaName)
      .setModelName(entity.getName())
      .setFullClassName(packageName + ".entity" + "." + StringUtils.capitalize(entity.getName()))
      .setOriginalModel(entity);

    for (TypedField<?, ?> field : entity.getFields()) {

      ModelField modelField = new ModelField()
        .setModelClass(modelClass)
        .setFieldName(field.getName())
        .setOriginalField(field)
        .setComment(field.getComment());

      if (field instanceof IDField idField) {
        TypeInfo typeInfo = TYPE_MAPPING.get(idField.getGeneratedValue().getType());

        modelField.setTypePackage(typeInfo.typePackage())
          .setShortTypeName(typeInfo.shortTypeName())
          .setFullTypeName(typeInfo.fullTypeName())
          .setIdentity(true);

        modelClass.setIdField(modelField);

      } else if (field instanceof RelationField relationField) {
        if (relationField.getCardinality() == ONE_TO_ONE) {
          modelField.setTypePackage(null)
            .setFullTypeName(modelField.getTypePackage() + "." + relationField.getTargetEntity())
            .setShortTypeName(relationField.getTargetEntity())
            .setRelationField(true);
          modelClass.getRelationFields().add(modelField);
        } else {
          modelField.setTypePackage("java.util")
            .setFullTypeName("java.util.List")
            .setShortTypeName("List<" + relationField.getTargetEntity() + ">")
            .setRelationField(true);
        }

      } else {

        TypeInfo typeInfo = TYPE_MAPPING.get(field.getType());
        modelField.setTypePackage(typeInfo.typePackage())
          .setShortTypeName(typeInfo.shortTypeName())
          .setFullTypeName(typeInfo.fullTypeName());
      }

      if (modelField.getTypePackage() != null) {
        modelClass.getImports().add(modelField.getFullTypeName());
      }
      modelClass.getAllFields().add(modelField);
    }
    return modelClass;
  }

}
