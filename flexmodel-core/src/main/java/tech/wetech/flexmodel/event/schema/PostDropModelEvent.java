package tech.wetech.flexmodel.event.schema;

import tech.wetech.flexmodel.Model;

/**
 * @author cjbi
 */
public class PostDropModelEvent extends AbstractSchemaEvent {


  private Model model;

  public PostDropModelEvent() {
  }

  public PostDropModelEvent(String schemaName, Model model) {
    this.schemaName = schemaName;
    this.model = model;
  }

  public Model getModel() {
    return model;
  }

  public void setModel(Model model) {
    this.model = model;
  }

}
