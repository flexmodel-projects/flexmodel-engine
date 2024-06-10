package tech.wetech.flexmodel.generator;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.mapping.TypeHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class DataValueGeneratorFacade {

  private final String schemaName;
  private final MappedModels mappedModels;
  private final Map<String, ? extends TypeHandler<?>> typeHandlerMap;

  public DataValueGeneratorFacade(AbstractSessionContext sessionContext) {
    this.schemaName = sessionContext.getSchemaName();
    this.mappedModels = sessionContext.getMappedModels();
    this.typeHandlerMap = sessionContext.getTypeHandlerMap();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Map<String, Object> generateValue(String modelName, Map<String, Object> data, boolean isUpdate) throws ValueGenerationException {
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    List<TypedField<?, ?>> fields = entity.getFields();
    Map<String, Object> newData = new HashMap<>();
    // 类型转换
    data.forEach((key, value) -> {
      TypedField field = (TypedField) entity.getField(key);
      if (field != null && !(field instanceof RelationField)) {
        newData.put(field.getName(), convertParameter(field, value)
        );
      }
    });
    for (TypedField<?, ?> field : fields) {
      if (field instanceof RelationField) {
        continue;
      }
      if (field.getGenerator() != null) {
        ValueGenerator generator = field.getGenerator();
        Runnable runnable = () -> newData.put(field.getName(), convertParameter(field, generator.generateValue(field, data)));
        switch (generator.getGenerationTime()) {
          case ALWAYS -> runnable.run();
          case INSERT -> {
            if (!isUpdate) {
              runnable.run();
            }
          }
          case UPDATE -> {
            if (isUpdate) {
              runnable.run();
            }
          }
          default -> throw new IllegalStateException("Unexpected value: " + generator.getGenerationTime());
        }

      }
    }
    return newData;
  }

  private Object convertParameter(TypedField<?, ?> field, Object value) {
    return getTypeHandler(field instanceof IDField idField ? idField.getGeneratedValue().getType() : field.getType())
      .convertParameter(value);
  }

  private TypeHandler<?> getTypeHandler(String type) {
    return typeHandlerMap.get(type);
  }


}
