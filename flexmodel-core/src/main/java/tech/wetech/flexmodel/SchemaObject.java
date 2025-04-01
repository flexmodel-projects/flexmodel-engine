package tech.wetech.flexmodel;

import java.io.Serializable;

/**
 * @author cjbi
 */
public interface SchemaObject extends Serializable {

    /**
     * 名称
     *
     * @return
     */
    String getName();

    /**
     * 类型
     *
     * @return
     */
    String getType();
}
