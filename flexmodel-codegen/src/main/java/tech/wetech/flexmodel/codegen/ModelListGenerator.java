package tech.wetech.flexmodel.codegen;

import java.io.File;

/**
 * @author cjbi
 */
public interface ModelListGenerator {

  File generate(ModelListGenerationContext context, String targetFile);

  String generate(ModelListGenerationContext context);

}
