package tech.wetech.flexmodel.codegen;

/**
 * @author cjbi
 */
public interface Generator {

  void generate(GenerationContext context);

  Configuration getConfiguration();

}
