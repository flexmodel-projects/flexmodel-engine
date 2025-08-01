package tech.wetech.flexmodel.core;

import tech.wetech.flexmodel.core.model.SchemaObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class ImportDescribe implements Serializable {

  private List<SchemaObject> schema = new ArrayList<>();
  private List<ImportData> data = new ArrayList<>();

  public List<SchemaObject> getSchema() {
    return schema;
  }

  public void setSchema(List<SchemaObject> schema) {
    this.schema = schema;
  }

  public List<ImportData> getData() {
    return data;
  }

  public void setData(List<ImportData> data) {
    this.data = data;
  }

  public static class ImportData implements Serializable {
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

    @Override
    public String toString() {
      return "ImportData{" +
             "modelName='" + modelName + '\'' +
             ", values=" + values +
             '}';
    }
  }

  @Override
  public String toString() {
    return "ImportDescribe{" +
           "schema=" + schema +
           ", data=" + data +
           '}';
  }
}
