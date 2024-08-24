package tech.wetech.flexmodel.codegen

import groovy.util.logging.Log

/**
 * @author cjbi
 */
@Log
class BuildItemSPIFileGenerator extends AbstractModelListGenerator {

  @Override
  def generate(PrintWriter out, ModelListGenerationContext context) {
    context.modelListClass.modelList.each {
      out.println "${it.packageName}.${context.schemaName.capitalize()}"
    }
  }
}
