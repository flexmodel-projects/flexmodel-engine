package tech.wetech.flexmodel.codegen

import groovy.io.GroovyPrintWriter

/**
 * @author cjbi
 */
class SchemaBuildItemGenerator implements MultipleModelGenerator {

  @Override
  void generate(MultipleModelGenerationContext context) {
    def className = "${context.schemaName.capitalize()}BuildItem"
    System.out.println "Generating: ${context.packageName}.${className}.java"
    new File(context.targetDirectory, "${className}.java").withPrintWriter { out ->
      generate(out as GroovyPrintWriter, className, context)
    }
  }

  def generate(GroovyPrintWriter out, String className, MultipleModelGenerationContext context) {
    out.println "package ${context.packageName};"
    out.println ""
    out.println "import tech.wetech.flexmodel.BuildItem;"
    out.println "import tech.wetech.flexmodel.Model;"
    out.println ""
    out.println "import java.util.List;"
    out.println ""
    out.println "public class $className implements BuildItem {"
    out.println ""
    out.println "  @Override"
    out.println "  public String getSchemaName() {"
    out.println "    return \"${context.schemaName}\";"
    out.println "  }"
    out.println ""
    out.println "  @Override"
    out.println "  public List<Model> getModels() {"
    out.println "    return Models.getAllModels();"
    out.println "  }"
    out.println ""
    out.println "}"
  }

}
