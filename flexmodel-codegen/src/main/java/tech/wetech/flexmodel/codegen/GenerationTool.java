package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.ModelImportBundle;

import java.io.File;

/**
 * 用于执行代码生成，允许通过命令行参数配置。
 *
 * @author cjbi
 */
public class GenerationTool {

  public static void run(Configuration configuration) {
    for (ModelImportBundle importDescribe : configuration.getImportDescribes()) {
      SchemaConfig schema = configuration.getSchemas().stream()
        .filter(s -> s.getName().equals(importDescribe.getSchemaName()))
        .findFirst()
        .orElseThrow();

      String packageName = schema.getPackageName();
      String targetDirectory = schema.getDirectory() + File.separator +
                               packageName.replace(".", File.separator);
      GenerationContext context = GenerationContext.buildGenerationContext(configuration, importDescribe);

      new PojoGenerator().generate(context, targetDirectory);
      new EnumGenerator().generate(context, targetDirectory);
//    new DSLGenerator().generate(context, targetDirectory);
      new SchemaGenerator().generate(context, targetDirectory);
      new BuildItemSPIFileGenerator().generate(context, schema.getBaseDir());

    }
  }
}

