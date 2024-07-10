package tech.wetech.flexmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class ScriptDescribe {

  private List<Model> schema = new ArrayList<>();
  private Map<String, List<Map<String, Object>>> data = new HashMap<>();

  public List<Model> getSchema() {
    return schema;
  }

  public void setSchema(List<Model> schema) {
    this.schema = schema;
  }

  public Map<String, List<Map<String, Object>>> getData() {
    return data;
  }

  public void setData(Map<String, List<Map<String, Object>>> data) {
    this.data = data;
  }
}
