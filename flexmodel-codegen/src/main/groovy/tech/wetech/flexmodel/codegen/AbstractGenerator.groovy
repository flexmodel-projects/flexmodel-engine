package tech.wetech.flexmodel.codegen

import groovy.io.GroovyPrintWriter
import groovy.util.logging.Log
import org.apache.groovy.io.StringBuilderWriter

/**
 * @author cjbi
 */
@Log
abstract class AbstractGenerator implements Generator {

  @Override
  File generate(GenerationContext context, String targetFile) {
    log.info "Generating: $targetFile"
    def file = new File(targetFile)
    if (!file.exists()) {
      file.getParentFile().mkdirs()
      file.createNewFile()
    }
    new File(targetFile).withPrintWriter { out ->
      writer(out, context)
    }
    return file
  }

  @Override
  String generate(GenerationContext context) {
    def writer = new StringBuilderWriter()
    writer(new GroovyPrintWriter(writer), context)
    return writer.toString()
  }

  abstract def writer(PrintWriter out, GenerationContext context)

}
