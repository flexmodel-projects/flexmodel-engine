package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.mapping.TypeHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class DataValueGenerator {

  private final String schemaName;
  private final MappedModels mappedModels;
  private final Map<String, ? extends TypeHandler<?>> typeHandlerMap;

  public DataValueGenerator(AbstractSessionContext sessionContext) {
    this.schemaName = sessionContext.getSchemaName();
    this.mappedModels = sessionContext.getMappedModels();
    this.typeHandlerMap = sessionContext.getTypeHandlerMap();
  }

  public Map<String, Object> generateAll(String modelName, Map<String, Object> data) throws ValueGenerateException {
    return generate(modelName, data, true);
  }

  public Map<String, Object> generate(String modelName, Map<String, Object> data) throws ValueGenerateException {
    return generate(modelName, data, false);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<String, Object> generate(String modelName, Map<String, Object> data, boolean calculateAll) throws ValueGenerateException {
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    List<TypedField<?, ?>> fields = entity.getFields();
    Map<String, Object> newData = new HashMap<>();
    for (TypedField<?, ?> field : fields) {
      if (field instanceof RelationField) {
        continue;
      }
      boolean flag = !calculateAll && !data.containsKey(field.getName());
      if (flag) {
        continue;
      }
      if (field.getGenerators().isEmpty() && data.containsKey(field.getName())) {
        newData.put(field.getName(), getTypeHandler(field instanceof IDField idField ? idField.getGeneratedValue().getType() : field.getType())
          .convertParameter(data.get(field.getName()))
        );
        continue;
      }
      for (ValueGenerator calculator : field.getGenerators()) {
        newData.put(field.getName(), getTypeHandler(field instanceof IDField idField ? idField.getGeneratedValue().getType() : field.getType())
          .convertParameter(calculator.generate(field, data))
        );
      }
    }
    return newData;
  }

  private TypeHandler<?> getTypeHandler(String type) {
    return typeHandlerMap.get(type);
  }


}
