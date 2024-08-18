package tech.wetech.flexmodel.codegen

import groovy.io.GroovyPrintWriter
import tech.wetech.flexmodel.JsonObjectConverter
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter

/**
 * @author cjbi
 */
class ModelsGenerator implements MultipleModelGenerator {

  @Override
  void generate(MultipleModelGenerationContext context) {
    System.out.println "Generating: ${context.packageName}.Models.java"
    new File(context.targetDirectory, "Models.java").withPrintWriter { out ->
      generate(out as GroovyPrintWriter, context)
    }
  }

  /**
   * Writes the Java class content to the GroovyPrintWriter.
   *
   * @param out Print writer to output the Java class code.
   * @param context The generation context with model details.
   */
  def generate(GroovyPrintWriter out, MultipleModelGenerationContext context) {
    def modelsClass = context.modelsClass
    JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

    out.println "package ${context.packageName};"
    out.println ""
    out.println "import tech.wetech.flexmodel.JsonObjectConverter;"
    out.println "import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;"
    out.println "import tech.wetech.flexmodel.Entity;"
    out.println ""
    modelsClass.imports.each(importStatement ->
      out.println "import ${importStatement};"
    )
    out.println ""

    // Write class-level comments
    out.println "/**"
    out.println " * Generated by Flexmodel Generator"
    out.println " */"

    out.println "public class Models {"

    modelsClass.models.each { model ->
      out.println ""
      if (model.comment) {
        out.println "  /**"
        out.println "   * ${model.comment}"
        out.println "   */"
      }
      out.println "  public static final Entity ${model.variableName};"
    }
    out.println ""
    out.println "  static {"
    out.println ""
    out.println "    JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();"
    modelsClass.models.each { model ->
      out.println "    ${model.variableName} = jsonObjectConverter.parseToObject(\"\"\""
      out.println "${jsonObjectConverter.toJsonString(model.originalModel)}"
      out.println "    \"\"\", Entity.class);"
    }
    out.println ""
    out.println "  }"
    out.println ""
    out.println "}"

  }

}
