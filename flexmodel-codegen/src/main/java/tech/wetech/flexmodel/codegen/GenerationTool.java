package tech.wetech.flexmodel.codegen;

import java.io.File;

/**
 * 用于执行代码生成，允许通过命令行参数配置。
 *
 * @author cjbi
 */
public class GenerationTool {

  public static void run(Configuration configuration) {
    String packageName = configuration.getTarget().getPackageName();
    String targetDirectory = configuration.getTarget().getDirectory() + File.separator +
                             packageName.replace(".", File.separator);
    GenerationContext context = GenerationContext.buildGenerationContext(configuration);

    new PojoGenerator().generate(context, targetDirectory);
    new DSLGenerator().generate(context, targetDirectory);
    new DAOGenerator().generate(context, targetDirectory);
    new EnumGenerator().generate(context, targetDirectory);
    new SchemaGenerator().generate(context, targetDirectory);
    new BuildItemSPIFileGenerator().generate(context, configuration.getTarget().getBaseDir());
  }

}
