package tech.wetech.flexmodel.codegen

import groovy.util.logging.Log

/**
 * @author cjbi
 */
@Log
class BuildItemSPIFileGenerator extends AbstractGenerator {

  @Override
  def write(PrintWriter out, GenerationContext context) {
    out.println "${context.packageName}.${context.schemaName.capitalize()}"
  }
}
