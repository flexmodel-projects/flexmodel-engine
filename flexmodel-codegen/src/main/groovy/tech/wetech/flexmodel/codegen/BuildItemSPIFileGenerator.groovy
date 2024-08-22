package tech.wetech.flexmodel.codegen

import groovy.util.logging.Log

/**
 * @author cjbi
 */
@Log
class BuildItemSPIFileGenerator implements MultipleModelGenerator {

  @Override
  void generate(MultipleModelGenerationContext context) {
    def fileName = "tech.wetech.flexmodel.BuildItem"
    log.info "Generating: $fileName"
    def parent = context.baseDir + "/target/classes/META-INF/services"

    new File(parent, fileName).withPrintWriter { out ->
      out.println "${context.packageName}.${context.schemaName.capitalize()}"
    }
  }

}
