package tech.wetech.flexmodel.codegen

import groovy.io.GroovyPrintWriter
import tech.wetech.flexmodel.JsonObjectConverter
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter

/**
 * @author cjbi
 */
class SchemaGenerator implements MultipleModelGenerator {

  @Override
  void generate(MultipleModelGenerationContext context) {
    def className = context.schemaName.capitalize()
    System.out.println "Generating: ${context.packageName}.${className}.java"
    new File(context.targetDirectory, "${className}.java").withPrintWriter { out ->
      generate(out as GroovyPrintWriter, className, context)
    }
  }

  /**
   * Writes the Java class content to the GroovyPrintWriter.
   *
   * @param out Print writer to output the Java class code.
   * @param context The generation context with model details.
   */
  def generate(GroovyPrintWriter out, String className, MultipleModelGenerationContext context) {
    def modelsClass = context.modelsClass
    JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

    out.println "package ${context.packageName};"
    out.println ""
    out.println "import tech.wetech.flexmodel.JsonObjectConverter;"
    out.println "import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;"
    out.println "import tech.wetech.flexmodel.BuildItem;"
    out.println "import tech.wetech.flexmodel.Entity;"
    out.println "import tech.wetech.flexmodel.Model;"
    out.println ""
    out.println "import java.util.ArrayList;"
    out.println "import java.util.List;"
    out.println ""
    modelsClass.imports.each(importStatement ->
      out.println "import ${importStatement};"
    )
    out.println ""

    // Write class-level comments
    out.println "/**"
    out.println " * Generated by Flexmodel Generator"
    out.println " */"

    out.println "public class ${className} implements BuildItem {"

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
    out.println ""
    out.println "  @Override"
    out.println "  public String getSchemaName() {"
    out.println "    return \"${context.schemaName}\";"
    out.println "  }"
    out.println ""
    out.println "  @Override"
    out.println "  public List<Model> getModels() {"
    out.println "    List<Model> list = new ArrayList<>();"
    modelsClass.models.each { model ->
      out.println "    list.add(${model.variableName});"
    }
    out.println "    return list;"
    out.println "  }"
    out.println ""
    out.println "}"

  }

}