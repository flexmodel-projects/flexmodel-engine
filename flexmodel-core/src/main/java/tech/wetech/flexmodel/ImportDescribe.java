package tech.wetech.flexmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class ImportDescribe {

  private List<TypeWrapper> schema = new ArrayList<>();
  private List<ImportData> data = new ArrayList<>();

  public List<TypeWrapper> getSchema() {
    return schema;
  }

  public void setSchema(List<TypeWrapper> schema) {
    this.schema = schema;
  }

  public List<ImportData> getData() {
    return data;
  }

  public void setData(List<ImportData> data) {
    this.data = data;
  }

  public static class ImportData {
    private String modelName;
    private List<Map<String, Object>> values;

    public String getModelName() {
      return modelName;
    }

    public void setModelName(String modelName) {
      this.modelName = modelName;
    }

    public List<Map<String, Object>> getValues() {
      return values;
    }

    public void setValues(List<Map<String, Object>> values) {
      this.values = values;
    }
  }

}
