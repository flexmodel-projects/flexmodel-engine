package tech.wetech.flexmodel.codegen

import groovy.util.logging.Log

/**
 * @author cjbi
 */
@Log
class BuildItemSPIFileGenerator extends AbstractModelListGenerator {

  @Override
  def generate(PrintWriter out, ModelListGenerationContext context) {
    out.println "${context.packageName}.${context.schemaName.capitalize()}"
  }
}
