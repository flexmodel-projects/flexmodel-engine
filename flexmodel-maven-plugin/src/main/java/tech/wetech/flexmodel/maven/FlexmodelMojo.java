package tech.wetech.flexmodel.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import static org.apache.maven.plugins.annotations.ResolutionScope.COMPILE;

/**
 * @author cjbi
 */
@Mojo(
  name = "exec",
  requiresDependencyResolution = COMPILE,
  defaultPhase = LifecyclePhase.VALIDATE,
  threadSafe = true
)
public class FlexmodelMojo extends AbstractMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

  }

}
