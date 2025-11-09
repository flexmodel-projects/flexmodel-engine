package tech.wetech.flexmodel.session;

import tech.wetech.flexmodel.query.DSL;
import tech.wetech.flexmodel.service.DataService;
import tech.wetech.flexmodel.service.SchemaService;
import tech.wetech.flexmodel.type.TypeHandler;

import java.util.Map;

/**
 * 统一的Session实现，完全合并了所有装饰器和中间层功能
 * 消除了所有中间层和装饰器，直接实现Session接口
 *
 * @author cjbi
 */
public abstract class AbstractSession implements Session {

    private final AbstractSessionContext sessionContext;
    private final DataService dataService;
    private final SchemaService schemaService;

    public AbstractSession(AbstractSessionContext sessionContext, DataService dataService,
                           SchemaService schemaService) {
        this.sessionContext = sessionContext;
        this.sessionContext.setSession(this);
        this.dataService = dataService;
        this.schemaService = schemaService;
    }

    @Override
    public DataService data() {
        return dataService;
    }

    @Override
    public SchemaService schema() {
        return schemaService;
    }

    @Override
    public SessionFactory getFactory() {
        return sessionContext.getFactory();
    }

  public Map<String, ? extends TypeHandler<?>> getTypeHandlerMap() {
    return sessionContext.getTypeHandlerMap();
  }

  @Override
    public String getName() {
        return sessionContext.getSchemaName();
    }

    @Override
  public DSL dsl() {
    return new DSL(this);
  }
}
