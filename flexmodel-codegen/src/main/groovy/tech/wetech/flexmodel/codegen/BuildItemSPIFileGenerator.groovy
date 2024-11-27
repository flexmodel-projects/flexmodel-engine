package tech.wetech.flexmodel.codegen

import groovy.util.logging.Log

/**
 * @author cjbi
 */
@Log
class BuildItemSPIFileGenerator extends AbstractModelListGenerator {

  @Override
  def generate(PrintWriter out, ModelListGenerationContext context) {
    context.modelListClass.each {
      out.println "${it.packageName}.${it.schemaName.capitalize()}"
    }
  }
}
