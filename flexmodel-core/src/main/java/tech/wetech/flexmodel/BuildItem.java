package tech.wetech.flexmodel;

import java.util.List;

/**
 * @author cjbi
 */
public interface BuildItem {

  String getSchemaName();

  /**
   * 获取模型
   *
   * @return
   */
  List<TypeWrapper> getSchema();

  List<ImportDescribe.ImportData> getData();
}
