package tech.wetech.flexmodel.codegen;

/**
 * @author cjbi
 */
public abstract class AbstractGenerator implements Generator {

  protected Configuration configuration;

  public Configuration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }

}
