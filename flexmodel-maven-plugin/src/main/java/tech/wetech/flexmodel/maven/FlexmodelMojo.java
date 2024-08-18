package tech.wetech.flexmodel.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import tech.wetech.flexmodel.codegen.Configuration;
import tech.wetech.flexmodel.codegen.GenerationTool;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

/**
 * Maven Mojo for executing the Flexmodel code generation tool.
 *
 * @author cjbi
 */
@Mojo(
  name = "generate",  // Renamed to be more descriptive
  requiresDependencyResolution = TEST,
  defaultPhase = LifecyclePhase.GENERATE_SOURCES,
  threadSafe = true
)
public class FlexmodelMojo extends AbstractMojo {

  /**
   * Maven project instance.
   */
  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  /**
   * Generator configuration.
   */
  @Parameter
  private Configuration generator;

  /**
   * Target directory for generated Java source files.
   */
  @Parameter(property = "generator.target.directory", defaultValue = "${project.build.directory}/generated-sources/flexmodel")
  private String outputDirectory;

  /**
   * Executes the Mojo.
   *
   * @throws MojoExecutionException if an error occurs while executing the Mojo
   * @throws MojoFailureException   if the Mojo execution fails
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Executing Flexmodel Maven Plugin");

    ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
    URLClassLoader pluginClassLoader = getClassLoader();

    try {

      Thread.currentThread().setContextClassLoader(pluginClassLoader);
      // Ensure the output directory is added as a compile source root
      getLog().info("Compile source root: " + outputDirectory);
      // Configure the generator to output to the specified directory
      if (generator != null) {
        generator.getTarget().setDirectory(outputDirectory);
        GenerationTool.run(generator);

      } else {
        getLog().warn("No generator configuration provided, skipping execution.");
      }
    } finally {
      project.addCompileSourceRoot(outputDirectory);
      Thread.currentThread().setContextClassLoader(oldCL);

      try {
        pluginClassLoader.close();
      }

      // Catch all possible errors to avoid suppressing the original exception
      catch (Throwable e) {
        getLog().error("Couldn't close the classloader.", e);
      }
    }

  }

  private URLClassLoader getClassLoader() throws MojoExecutionException {
    try {
      List<String> classpathElements = project.getRuntimeClasspathElements();
      URL urls[] = new URL[classpathElements.size()];

      for (int i = 0; i < urls.length; i++)
        urls[i] = new File(classpathElements.get(i)).toURI().toURL();

      return new URLClassLoader(urls, getClass().getClassLoader());
    } catch (Exception e) {
      throw new MojoExecutionException("Couldn't create a classloader.", e);
    }
  }

}
