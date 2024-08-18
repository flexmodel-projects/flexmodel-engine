package tech.wetech.flexmodel.codegen
/**
 * @author cjbi
 */
class BuildItemSPIFileGenerator implements MultipleModelGenerator {

  @Override
  void generate(MultipleModelGenerationContext context) {
    def fileName = "tech.wetech.flexmodel.BuildItem"
    System.out.println "Generating: $fileName"
    def parent = context.baseDir + "/classes/META-INF/services"

    new File(parent, fileName).withPrintWriter { out ->
      out.println "${context.packageName}.${context.schemaName.capitalize()}"
    }
  }

}
