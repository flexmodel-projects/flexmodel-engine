package tech.wetech.flexmodel.codegen;

import tech.wetech.flexmodel.ModelImportBundle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于执行代码生成，允许通过命令行参数配置。
 *
 * @author cjbi
 */
public class GenerationTool {

  public static void run(Configuration configuration) {
    List<String> buildItems = new ArrayList<>();
    for (ModelImportBundle importDescribe : configuration.getImportDescribes()) {
      SchemaConfig schema = configuration.getSchemas().stream()
        .filter(s -> s.getName().equals(importDescribe.getSchemaName()))
        .findFirst()
        .orElseThrow();

      String packageName = schema.getPackageName();
      String targetDirectory = schema.getDirectory() + File.separator +
                               packageName.replace(".", File.separator);
      GenerationContext context = GenerationContext.buildGenerationContext(configuration, importDescribe);
      buildItems.add(context.getPackageName() + "." + StringUtils.capitalize(StringUtils.snakeToCamel(context.getSchemaName())));
      new PojoGenerator().generate(context, targetDirectory);
      new EnumGenerator().generate(context, targetDirectory);
      new SchemaGenerator().generate(context, targetDirectory);
      context.putVariable("buildItems", buildItems);
      new BuildItemSPIFileGenerator().generate(context, schema.getBaseDir());
    }
  }
}

