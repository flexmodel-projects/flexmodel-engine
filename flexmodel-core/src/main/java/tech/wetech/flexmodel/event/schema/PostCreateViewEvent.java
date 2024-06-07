package tech.wetech.flexmodel.event.schema;

import tech.wetech.flexmodel.View;

/**
 * @author cjbi
 */
public class PostCreateViewEvent extends AbstractSchemaEvent {

  private View view;

  public PostCreateViewEvent() {
  }

  public PostCreateViewEvent(String schemaName, View view) {
    this.schemaName = schemaName;
    this.view = view;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public View getView() {
    return view;
  }

  public void setView(View view) {
    this.view = view;
  }
}
