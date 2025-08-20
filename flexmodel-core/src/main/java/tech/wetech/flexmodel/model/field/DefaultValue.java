package tech.wetech.flexmodel.model.field;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 统一处理字段默认值的类
 * 支持固定值：{"type":"fixed","value":"xxx"}
 * 支持生成值：{"type":"generated","name":"uuid"}
 *
 * @author cjbi
 */
public class DefaultValue implements Serializable {

    /**
     * 数据库自增
     */
    public static final DefaultValue AUTO_INCREMENT = DefaultValue.generated("autoIncrement");
    /**
     * UUID
     */
    public static final DefaultValue UUID = DefaultValue.generated("uuid");
    /**
     * ULID
     */
    public static final DefaultValue ULID = DefaultValue.generated("ulid");
    /**
     * 当前值
     */
    public static final DefaultValue NOW = DefaultValue.generated("now");


    @Serial
    private static final long serialVersionUID = 1L;

    private String type;
    private Object value;
    private String name;

    public DefaultValue() {
    }

    /**
     * 创建固定值类型的默认值
     */
    public static DefaultValue fixed(Object value) {
        DefaultValue defaultValue = new DefaultValue();
        defaultValue.type = "fixed";
        defaultValue.value = value;
        return defaultValue;
    }

    /**
     * 创建生成值类型的默认值
     */
    public static DefaultValue generated(String name) {
        DefaultValue defaultValue = new DefaultValue();
        defaultValue.type = "generated";
        defaultValue.name = name;
        return defaultValue;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public boolean isFixed() {
        return "fixed".equals(type);
    }

    public boolean isGenerated() {
        return "generated".equals(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultValue that)) return false;
        return type == that.type &&
               Objects.equals(value, that.value) &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, name);
    }

    @Override
    public String toString() {
        if (isFixed()) {
            return "DefaultValue{type=fixed, value=" + value + "}";
        } else if (isGenerated()) {
            return "DefaultValue{type=generated, name='" + name + "'}";
        }
        return "DefaultValue{type=null}";
    }
}
