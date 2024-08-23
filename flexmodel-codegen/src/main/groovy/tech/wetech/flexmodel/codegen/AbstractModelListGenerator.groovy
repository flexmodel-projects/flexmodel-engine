package tech.wetech.flexmodel.codegen

import groovy.io.GroovyPrintWriter
import groovy.util.logging.Log
import org.apache.groovy.io.StringBuilderWriter

/**
 * @author cjbi
 */
@Log
abstract class AbstractModelListGenerator implements ModelListGenerator {

  @Override
  File generate(ModelListGenerationContext context, String targetFile) {
    log.info "Generating: $targetFile"
    def file = new File(targetFile)
    new File(targetFile).withPrintWriter { out ->
      generate(out, context)
    }
    return file
  }

  @Override
  String generate(ModelListGenerationContext context) {
    def writer = new StringBuilderWriter()
    generate(new GroovyPrintWriter(writer), context)
    return writer.toString()
  }

  abstract def generate(PrintWriter out, ModelListGenerationContext context)
}
