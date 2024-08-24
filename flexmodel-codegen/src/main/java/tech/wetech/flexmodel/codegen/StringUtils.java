package tech.wetech.flexmodel.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author cjbi
 */
public class StringUtils {

  public static String capitalize(final CharSequence self) {
    if (self.length() == 0) return "";
    return "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
  }

  /**
   * Convenience method to uncapitalize the first letter of a CharSequence
   * (typically the first letter of a word). Example usage:
   * <pre class="groovyTestCase">
   * assert 'H'.uncapitalize() == 'h'
   * assert 'Hello'.uncapitalize() == 'hello'
   * assert 'Hello world'.uncapitalize() == 'hello world'
   * assert 'Hello World'.uncapitalize() == 'hello World'
   * assert 'hello world' == 'Hello World'.split(' ').collect{ it.uncapitalize() }.join(' ')
   * </pre>
   *
   * @param self The CharSequence to uncapitalize
   * @return A String containing the uncapitalized toString() of the CharSequence
   * @since 2.4.8
   */
  public static String uncapitalize(final CharSequence self) {
    if (self.length() == 0) return "";
    return "" + Character.toLowerCase(self.charAt(0)) + self.subSequence(1, self.length());
  }

  static void createDirectoriesIfNotExists(String targetDirectory) {
    try {
      Path path = Paths.get(targetDirectory);
      Files.createDirectories(path);
    } catch (IOException e) {
      System.err.println("Error creating directories: " + e.getMessage());
    }
  }
}
