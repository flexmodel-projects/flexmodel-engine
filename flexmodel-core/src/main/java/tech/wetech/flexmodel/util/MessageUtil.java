package tech.wetech.flexmodel.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author cjbi
 */
public class MessageUtil {

  public static String getString(String key, Object... params) {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("message");
    return MessageFormat.format(resourceBundle.getString(key), params);
  }

  public static void main(String[] args) {
    // 创建一个 I18nUtil 实例，指定资源路径和区域设置
    // 获取国际化字符串
    System.out.println(MessageUtil.getString("greeting")); // 输出：你好
    System.out.println(MessageUtil.getString("farewell")); // 输出：再见
    System.out.println("你好"); // 输出：再见

    // 示例：格式化字符串
    String name = "张三";
    System.out.println(MessageUtil.getString("farewell_1", name));
  }

}
