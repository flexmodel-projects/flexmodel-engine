package tech.wetech.flexmodel.graphql;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author cjbi
 */
public class I18nUtil {

    private final ResourceBundle bundle;

    public I18nUtil() {
        this.bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
    }

    public String getString(String key, Object... params) {
        return MessageFormat.format(bundle.getString(key), params);
    }

    public static void main(String[] args) {
        // 创建一个 I18nUtil 实例，指定资源路径和区域设置
        // 获取国际化字符串
        I18nUtil i18n = new I18nUtil();
        System.out.println(i18n.getString("greeting")); // 输出：你好
        System.out.println(i18n.getString("farewell")); // 输出：再见
        System.out.println("你好"); // 输出：再见

        // 示例：格式化字符串
        String name = "张三";
        System.out.println(i18n.getString("farewell_1", name));
    }

}
