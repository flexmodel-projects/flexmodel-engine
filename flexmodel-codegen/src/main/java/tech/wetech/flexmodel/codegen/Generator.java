package tech.wetech.flexmodel.codegen;

import java.io.File;
import java.util.List;

/**
 * @author cjbi
 */
public interface Generator {

  List<File> generate(GenerationContext context, String dir);

  List<String> generate(GenerationContext context);

}
