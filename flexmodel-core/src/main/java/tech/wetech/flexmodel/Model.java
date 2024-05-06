package tech.wetech.flexmodel;

import java.io.Serializable;
import java.util.List;

/**
 * @author cjbi
 */
public interface Model extends Serializable {

  String name();

  List<? extends Field> fields();

}
