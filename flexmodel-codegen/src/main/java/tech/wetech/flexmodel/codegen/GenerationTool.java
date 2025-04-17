package tech.wetech.flexmodel.codegen;

import groovy.text.GStringTemplateEngine;
import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.parser.ASTNodeConverter;
import tech.wetech.flexmodel.parser.impl.ModelParser;
import tech.wetech.flexmodel.parser.impl.ParseException;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
    TYPE_MAPPING.put(RELATION.getType(), null);
    TYPE_MAPPING.put(STRING.getType(), new TypeInfo(null, "String", "String"));
    TYPE_MAPPING.put(FLOAT.getType(), new TypeInfo(null, "Double", "Double"));
    TYPE_MAPPING.put(INT.getType(), new TypeInfo(null, "Integer", "Integer"));
    TYPE_MAPPING.put(LONG.getType(), new TypeInfo(null, "Long", "Long"));
    TYPE_MAPPING.put(BOOLEAN.getType(), new TypeInfo(null, "Boolean", "Boolean"));
    TYPE_MAPPING.put(DATETIME.getType(), new TypeInfo("java.time", "LocalDateTime", "java.time.LocalDateTime"));
    TYPE_MAPPING.put(DATE.getType(), new TypeInfo("java.time", "LocalDate", "java.time.LocalDate"));
    TYPE_MAPPING.put(TIME.getType(), new TypeInfo("java.time", "LocalTime", "java.time.LocalTime"));
    TYPE_MAPPING.put(JSON.getType(), new TypeInfo(null, "Object", "Object"));
  }

  public static void runV2(Configuration configuration) throws Exception {
    ImportDescribe iDesc = getImportDescribe(configuration);
    Configuration.Schema schema = configuration.getSchema();
    String packageName = configuration.getTarget().getPackageName();
    String targetDirectory = configuration.getTarget().getDirectory() + File.separator;

    GenerationContext context = new GenerationContext();
    context.setSchemaName(schema.getName());
    context.setPackageName(packageName);

    for (SchemaObject model : iDesc.getSchema()) {
      if (model instanceof Entity) {
        context.getModelClassList().add(buildModelClass(configuration.getTarget().getReplaceString(), packageName, schema.getName(), (Entity) model));
      } else if (model instanceof Enum) {
        context.getEnumClassList().add(buildEnumClass(packageName, configuration.getSchema().getName(), (Enum) model));
      }
    }
    GStringTemplateEngine engine = new GStringTemplateEngine();
    File templateDir = new File(Objects.requireNonNull(GenerationTool.class.getClassLoader().getResource("templates/")).toURI());
    outputFiles(engine, context, new File(Objects.requireNonNull(GenerationTool.class.getClassLoader().getResource("templates/")).toURI()), templateDir.getAbsolutePath(), targetDirectory);
  }

  public static void outputFiles(GStringTemplateEngine engine, GenerationContext context, File dir, String sourceDirectory, String targetDirectory) throws Exception {
    File[] files = dir.listFiles();
    if (files == null) return;


    for (File file : files) {
      try {
        if (file.isDirectory()) {
          File targetDir = getTargetFile(engine, context, sourceDirectory, targetDirectory, file);
          targetDir.mkdirs();
          outputFiles(engine, context, file, sourceDirectory, targetDirectory); // 递归遍历子目录
        } else {

          if (file.getName().endsWith(".each_entity.template")) {
            while (context.nextModel()) {
              File targetFile = getTargetFile(engine, context, sourceDirectory, targetDirectory, file);
              targetFile.createNewFile();
              FileWriter fileWriter = new FileWriter(targetFile);
              engine.createTemplate(file).make(new JacksonObjectConverter().convertValue(context, Map.class)).writeTo(fileWriter);
              fileWriter.flush();
              fileWriter.close();
            }
          } else if (file.getName().endsWith(".each_enum.template")) {
            while (context.nextEnum()) {
              File targetFile = getTargetFile(engine, context, sourceDirectory, targetDirectory, file);
              targetFile.createNewFile();
              FileWriter fileWriter = new FileWriter(targetFile);
              engine.createTemplate(file).make(new JacksonObjectConverter().convertValue(context, Map.class)).writeTo(fileWriter);
              fileWriter.flush();
              fileWriter.close();
            }
          } else if (file.getName().endsWith(".template")) {
            context.cleanIte();
            File targetFile = getTargetFile(engine, context, sourceDirectory, targetDirectory, file);
            targetFile.createNewFile();
            FileWriter fileWriter = new FileWriter(targetFile);
            engine.createTemplate(file).make(new JacksonObjectConverter().convertValue(context, Map.class)).writeTo(fileWriter);
            fileWriter.flush();
            fileWriter.close();
          } else {

          }
        }
      } catch (Exception e) {
        System.err.println("Generate file error, file:" + file);
        throw e;
      }
    }


  }

  private static File getTargetFile(GStringTemplateEngine engine, GenerationContext context, String sourceDirectory, String targetDirectory, File file) throws ClassNotFoundException, IOException {
    if (file.isDirectory()) {
      String filePath = engine.createTemplate(file.getAbsolutePath().replace("\\", "/"))
        .make(new JacksonObjectConverter().convertValue(context, Map.class)).toString();
      String targetPath = filePath.replace(sourceDirectory.replace("\\", "/"), targetDirectory.replace("\\", "/"))
        .replace("\\", "/");
      return new File(targetPath);
    } else {
      String filePath = engine.createTemplate(file.getAbsolutePath().replace("\\", "/"))
        .make(new JacksonObjectConverter().convertValue(context, Map.class)).toString();
      String targetPath = filePath.replace(sourceDirectory.replace("\\", "/"), targetDirectory.replace("\\", "/"))
        .replace("\\", "/");
      targetPath = targetPath.replaceAll("\\.each_entity\\.template|\\.each_enum\\.template|\\.template", "");
      return new File(targetPath);
    }

  }


  private static ImportDescribe getImportDescribe(Configuration configuration) {
    JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();
    List<SchemaObject> models = new ArrayList<>();
    List<ImportDescribe.ImportData> data = new ArrayList<>();
    // read from script
    String[] importScripts = configuration.getSchema().getImportScript().split(",");
    for (String importScript : importScripts) {
      File scriptFile = Path.of(configuration.getTarget().getBaseDir(), importScript).toFile();
      System.out.println("Import Script File Path: " + scriptFile.getAbsolutePath());
      if (scriptFile.exists()) {
        System.out.println("Script file is exists, import Script File: " + scriptFile);
        if (importScript.endsWith(".json")) {
          try {
            String content = Files.readString(scriptFile.toPath());
            ImportDescribe describe = jsonObjectConverter.parseToObject(content, ImportDescribe.class);
            models.addAll(describe.getSchema());
            data.addAll(describe.getData());
          } catch (IOException e) {
            System.out.println("Parse file error: " + importScript);
            throw new RuntimeException(e);
          }
        } else if (importScript.endsWith(".idl")) {
          try {
            String content = Files.readString(scriptFile.toPath());
            ModelParser modelParser = new ModelParser(new ByteArrayInputStream(content.getBytes()));
            List<ModelParser.ASTNode> list = modelParser.CompilationUnit();
            for (ModelParser.ASTNode astNode : list) {
              models.add(ASTNodeConverter.toSchemaObject(astNode));
            }
          } catch (IOException | ParseException e) {
            System.out.println("Parse file error: " + importScript);
            throw new RuntimeException(e);
          }
        } else {
          System.out.println("Unsupported script file type: " + importScript + ", must be .json or .idl");
        }
      }
    }
    ImportDescribe importDescribe = new ImportDescribe();
    importDescribe.setSchema(models);
    importDescribe.setData(data);
    return importDescribe;
  }

  public static void run(Configuration configuration) {
    Configuration.Schema schema = configuration.getSchema();
    JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();
    List<SchemaObject> models = new ArrayList<>();
    List<ImportDescribe.ImportData> data = new ArrayList<>();
    // read from script
    String[] importScripts = configuration.getSchema().getImportScript().split(",");
    for (String importScript : importScripts) {
      File scriptFile = Path.of(configuration.getTarget().getBaseDir(), importScript).toFile();
      System.out.println("Import Script File Path: " + scriptFile.getAbsolutePath());
      if (scriptFile.exists()) {
        System.out.println("Script file is exists, import Script File: " + scriptFile);
        if (importScript.endsWith(".json")) {
          try {
            String content = Files.readString(scriptFile.toPath());
            ImportDescribe describe = jsonObjectConverter.parseToObject(content, ImportDescribe.class);
            models.addAll(describe.getSchema());
            data.addAll(describe.getData());
          } catch (IOException e) {
            System.out.println("Parse file error: " + importScript);
            throw new RuntimeException(e);
          }
        } else if (importScript.endsWith(".idl")) {
          try {
            String content = Files.readString(scriptFile.toPath());
            ModelParser modelParser = new ModelParser(new ByteArrayInputStream(content.getBytes()));
            List<ModelParser.ASTNode> list = modelParser.CompilationUnit();
            for (ModelParser.ASTNode astNode : list) {
              models.add(ASTNodeConverter.toSchemaObject(astNode));
            }
          } catch (IOException | ParseException e) {
            System.out.println("Parse file error: " + importScript);
            throw new RuntimeException(e);
          }
        } else {
          System.out.println("Unsupported script file type: " + importScript + ", must be .json or .idl");
        }
      }
    }

    String packageName = configuration.getTarget().getPackageName();
    String targetDirectory = configuration.getTarget().getDirectory() + File.separator +
                             packageName.replace(".", File.separator);
    GenerationContext context = buildGenerationContext(configuration, models);
    context.putVariable("rootPackage", packageName);
    context.putVariable("import_data", data);

    while (context.nextModel()) {
      ModelClass modelClass = context.getModelClass();
      new PojoGenerator().generate(context, Path.of(targetDirectory, "entity", modelClass.getShortClassName() + ".java").toString());
      new DSLGenerator().generate(context, Path.of(targetDirectory, "dsl", modelClass.getShortClassName() + "DSL.java").toString());
      new DAOGenerator().generate(context, Path.of(targetDirectory, "dao", modelClass.getShortClassName() + "DAO.java").toString());
    }
    while (context.nextEnum()) {
      EnumGenerator enumGenerator = new EnumGenerator();
      EnumClass enumClass = context.getEnumClass();
      enumGenerator.generate(context, Path.of(targetDirectory, "enumeration", enumClass.getShortClassName() + ".java").toString());
    }

    // generate multiple model file

    SchemaGenerator schemaClassGenerator = new SchemaGenerator();

    schemaClassGenerator.generate(context, Path.of(targetDirectory, StringUtils.capitalize(schema.getName()) + ".java").toString());

    BuildItemSPIFileGenerator buildItemSPIFileGenerator = new BuildItemSPIFileGenerator();
    buildItemSPIFileGenerator.generate(context, Path.of(
      configuration.getTarget().getBaseDir(),
      "target/classes/META-INF/services",
      "tech.wetech.flexmodel.BuildItem"
    ).toString());
  }

  private static GenerationContext buildGenerationContext(Configuration configuration, List<SchemaObject> models) {
    Configuration.Schema schema = configuration.getSchema();
    String packageName = configuration.getTarget().getPackageName();
    Map<String, ModelClass> modelClassMap = new HashMap<>();
    Map<String, EnumClass> enumClassMap = new HashMap<>();
    for (SchemaObject model : models) {
      if (model instanceof Entity) {
        modelClassMap.put(model.getName(), buildModelClass(configuration.getTarget().getReplaceString(), packageName, schema.getName(), (Entity) model));
      } else if (model instanceof Enum) {
        enumClassMap.put(model.getName(), buildEnumClass(packageName, schema.getName(), (Enum) model));
      }
    }

    GenerationContext context = new GenerationContext();
    context.setSchemaName(schema.getName());
    context.setPackageName(packageName);
    for (SchemaObject model : models) {
      if (model instanceof Entity) {
        ModelClass modelClass = modelClassMap.get(model.getName());
        context.getModelClassList().add(modelClass);
        context.getImports().add(modelClass.getFullClassName());
      } else if (model instanceof Enum) {
        EnumClass enumClass = enumClassMap.get(model.getName());
        context.getEnumClassList().add(enumClass);
        context.getImports().add(enumClass.getFullClassName());
      }
    }
    return context;
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
      .setShortClassName(StringUtils.capitalize(cCamelName))
      .setPackageName(packageName + ".entity")
      .setSchemaName(schemaName)
      .setModelName(entity.getName())
      .setFullClassName(packageName + ".entity" + "." + StringUtils.capitalize(cCamelName))
      .setOriginalModel(entity);

    for (TypedField<?, ?> field : entity.getFields()) {

      ModelField modelField = new ModelField()
        .setModelClass(modelClass)
        .setIdentity(field.isIdentity())
        .setVariableName(StringUtils.snakeToCamel(field.getName()))
        .setFieldName(field.getName())
        .setOriginalField(field)
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

}
