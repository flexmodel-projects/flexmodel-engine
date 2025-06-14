package tech.wetech.flexmodel.codegen

import groovy.util.logging.Log

import java.nio.file.Path

/**
 * @author cjbi
 */
@Log
class BuildItemSPIFileGenerator extends AbstractGenerator {

  @Override
  String getTargetFile(GenerationContext context, String targetDirectory) {
    return Path.of(
      targetDirectory,
      "target/classes/META-INF/services",
      "tech.wetech.flexmodel.BuildItem"
    ).toString()
  }

  @Override
  void write(PrintWriter out, GenerationContext context) {
    out.println "${context.packageName}.${context.schemaName.capitalize()}"
  }
}
