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

  /**
   * 将下划线命名转换为驼峰命名
   * @param s 下划线命名字符串
   * @return 转换后的驼峰命名字符串
   */
  public static String snakeToCamel(String s) {
    if (s == null || !s.contains("_")){
      return s;
    }
    StringBuffer sb = new StringBuffer();
    //用来判断大写的标志
    boolean nextUpperCase = false;
    for (int i = 0; i < s.length(); i++) {
      if ("_".equals(String.valueOf(s.charAt(i)))) {
        nextUpperCase = true;
      } else {
        if (nextUpperCase) {
          sb = sb.append(String.valueOf(s.charAt(i)).toUpperCase());
          nextUpperCase = false;
        }else {
          sb = sb.append(s.charAt(i));
        }
      }
    }
    return sb.toString();
  }

}
