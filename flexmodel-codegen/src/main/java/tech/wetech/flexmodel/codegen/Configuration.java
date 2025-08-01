package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.core.ImportDescribe;
import tech.wetech.flexmodel.core.JsonObjectConverter;
import tech.wetech.flexmodel.core.SchemaObject;
import tech.wetech.flexmodel.core.parser.ASTNodeConverter;
import tech.wetech.flexmodel.core.parser.impl.ModelParser;
import tech.wetech.flexmodel.core.parser.impl.ParseException;
import tech.wetech.flexmodel.core.supports.jackson.JacksonObjectConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置类，用于存储和处理代码生成的各种设置，包括数据库连接信息和生成策略。
 *
 * @author cjbi
 */
public class Configuration implements Serializable {

  private Schema schema;
  private Target target;

  public Schema getSchema() {
    return schema;
  }

  public void setSchema(Schema schema) {
    this.schema = schema;
  }

  public Target getTarget() {
    return target;
  }

  public void setTarget(Target target) {
    this.target = target;
  }

  public ImportDescribe getImportDescribe() {
    JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();
    List<SchemaObject> models = new ArrayList<>();
    List<ImportDescribe.ImportData> data = new ArrayList<>();
    // read from script
    String[] importScripts = schema.getImportScript().split(",");
    for (String importScript : importScripts) {
      File scriptFile = Path.of(target.getBaseDir(), importScript).toFile();
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

}
