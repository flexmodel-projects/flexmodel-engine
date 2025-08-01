package tech.wetech.flexmodel.core;

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
  List<SchemaObject> getSchema();

  List<ImportDescribe.ImportData> getData();
}
