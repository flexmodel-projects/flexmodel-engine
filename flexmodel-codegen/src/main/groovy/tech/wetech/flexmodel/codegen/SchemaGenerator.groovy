package tech.wetech.flexmodel.codegen

import tech.wetech.flexmodel.JsonObjectConverter
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter

/**
 * @author cjbi
 */
class SchemaGenerator extends AbstractModelListGenerator {

  /**
   * Writes the Java class content to the GroovyPrintWriter.
   *
   * @param out Print writer to output the Java class code.
   * @param context The generation context with model details.
   */
  @Override
  def generate(PrintWriter out, ModelListGenerationContext context) {
    def modelListClass = context.modelListClass
    def className = modelListClass.schemaName.capitalize()
    JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

    out.println "package ${context.modelListClass.packageName};"
    out.println ""
    out.println "import tech.wetech.flexmodel.JsonObjectConverter;"
    out.println "import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;"
    out.println "import tech.wetech.flexmodel.BuildItem;"
    out.println "import tech.wetech.flexmodel.Entity;"
    out.println "import tech.wetech.flexmodel.TypeWrapper;"
    out.println ""
    out.println "import java.util.ArrayList;"
    out.println "import java.util.List;"
    out.println ""
    modelListClass.imports.each(importStatement ->
      out.println "import ${importStatement};"
    )
    out.println ""

    // Write class-level comments
    out.println "/**"
    out.println " * Generated by Flexmodel Generator"
    out.println " */"

    out.println "public class ${className} implements BuildItem {"

    modelListClass.modelList.each { model ->
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
    modelListClass.modelList.each { model ->
      out.println "    ${model.variableName} = jsonObjectConverter.parseToObject(\"\"\""
      out.println "    ${jsonObjectConverter.toJsonString(model.originalModel)}"
      out.println "    \"\"\", Entity.class);"
    }
    out.println ""
    out.println "  }"
    out.println ""
    out.println ""
    out.println "  @Override"
    out.println "  public String getSchemaName() {"
    out.println "    return \"${context.modelListClass.schemaName}\";"
    out.println "  }"
    out.println ""
    out.println "  @Override"
    out.println "  public List<TypeWrapper> getModels() {"
    out.println "    List<TypeWrapper> list = new ArrayList<>();"
    modelListClass.modelList.each { model ->
      out.println "    list.add(${model.variableName});"
    }
    out.println "    return list;"
    out.println "  }"
    out.println ""
    out.println "}"

  }

}
