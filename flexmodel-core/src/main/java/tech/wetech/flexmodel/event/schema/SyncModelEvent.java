package tech.wetech.flexmodel.event.schema;

/**
 * @author cjbi
 */
public class SyncModelEvent extends AbstractSchemaEvent {

  public SyncModelEvent() {
  }

  public SyncModelEvent(String schemaName) {
    this.schemaName = schemaName;
  }

}
