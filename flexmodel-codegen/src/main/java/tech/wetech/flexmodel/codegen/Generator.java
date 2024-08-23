package tech.wetech.flexmodel.codegen;

import java.io.File;

/**
 * @author cjbi
 */
public interface Generator {

  File generate(GenerationContext context, String targetFile);

  String generate(GenerationContext context);

}
