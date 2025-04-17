package tech.wetech.flexmodel.codegen

import groovy.util.logging.Log

/**
 * @author cjbi
 */
@Log
class BuildItemSPIFileGenerator extends AbstractGenerator {

  @Override
  def writer(PrintWriter out, GenerationContext context) {
    context.modelClassList.each {
      out.println "${it.packageName}.${it.schemaName.capitalize()}"
    }
  }
}
