package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.sql.JdbcMappedModels;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  public static void main(String[] args) {
    run(null);
  }

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
    File scriptFile = new File(configuration.getTarget().getBaseDir() +"/src/main/resources/" + importScript);
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
    pojoGenerator.setConfiguration(configuration);
    DaoGenerator daoGenerator = new DaoGenerator();
    daoGenerator.setConfiguration(configuration);
    String packageName = configuration.getTarget().getPackageName();
    String targetDirectory = configuration.getTarget().getDirectory() + File.separator +
                             packageName.replace(".", File.separator);
    createDirectoriesIfNotExists(targetDirectory + File.separator + "dao");
    createDirectoriesIfNotExists(targetDirectory + File.separator + "entity");
    // generate single model file
    for (Model model : models) {
      GenerationContext context = new GenerationContext();
      context.setSchemaName(schema.getName());
      context.setModelClass(buildModelClass(packageName, (Entity) model));
      context.setPackageName(packageName);
      context.setBaseDir(configuration.getTarget().getBaseDir());
      context.setTargetDirectory(targetDirectory);
      pojoGenerator.generate(context);
      daoGenerator.generate(context);
    }
    // generate multiple model file
    MultipleModelGenerationContext multipleModelGenerationContext = new MultipleModelGenerationContext();
    multipleModelGenerationContext.setSchemaName(schema.getName());
    multipleModelGenerationContext.setTargetDirectory(targetDirectory);
    multipleModelGenerationContext.setBaseDir(configuration.getTarget().getBaseDir());
    multipleModelGenerationContext.setPackageName(packageName);
    MultipleModelClass multipleModelClass = new MultipleModelClass();
    multipleModelClass.setPackageName(packageName);
    multipleModelGenerationContext.setModelsClass(multipleModelClass);
    for (Model model : models) {
      ModelClass modelClass = buildModelClass(packageName, (Entity) model);
      multipleModelClass.getModels().add(modelClass);
      multipleModelClass.getImports().add(modelClass.getFullClassName());
    }

    SchemaGenerator modelsGenerator = new SchemaGenerator();
    modelsGenerator.generate(multipleModelGenerationContext);

    createDirectoriesIfNotExists(multipleModelGenerationContext.getBaseDir() + "/target/classes/META-INF/services");
    BuildItemSPIFileGenerator buildItemSPIFileGenerator = new BuildItemSPIFileGenerator();
    buildItemSPIFileGenerator.generate(multipleModelGenerationContext);
  }

  private static ModelClass buildModelClass(String packageName, Entity entity) {

    ModelClass modelClass = new ModelClass()
      .setComment(entity.getComment())
      .setVariableName(uncapitalize(entity.getName()))
      .setLowerCaseName(uncapitalize(entity.getName()))
      .setShortClassName(capitalize(entity.getName()))
      .setPackageName(packageName + ".entity")
      .setFullClassName(packageName + ".entity" + "." + capitalize(entity.getName()))
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
            .setShortTypeName(relationField.getTargetEntity());
        } else {
          modelField.setTypePackage("java.util")
            .setFullTypeName("java.util.List")
            .setShortTypeName("List<" + relationField.getTargetEntity() + ">");
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

  public static String capitalize(final CharSequence self) {
    if (self.length() == 0) return "";
    return "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
  }

  /**
   * Convenience method to uncapitalize the first letter of a CharSequence
   * (typically the first letter of a word). Example usage:
   * <pre class="groovyTestCase">
   * assert 'H'.uncapitalize() == 'h'
   * assert 'Hello'.uncapitalize() == 'hello'
   * assert 'Hello world'.uncapitalize() == 'hello world'
   * assert 'Hello World'.uncapitalize() == 'hello World'
   * assert 'hello world' == 'Hello World'.split(' ').collect{ it.uncapitalize() }.join(' ')
   * </pre>
   *
   * @param self The CharSequence to uncapitalize
   * @return A String containing the uncapitalized toString() of the CharSequence
   * @since 2.4.8
   */
  public static String uncapitalize(final CharSequence self) {
    if (self.length() == 0) return "";
    return "" + Character.toLowerCase(self.charAt(0)) + self.subSequence(1, self.length());
  }

  private static void createDirectoriesIfNotExists(String targetDirectory) {
    try {
      Path path = Paths.get(targetDirectory);
      Files.createDirectories(path);
    } catch (IOException e) {
      System.err.println("Error creating directories: " + e.getMessage());
    }
  }
}
