package tech.wetech.flexmodel.generator;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.reflect.ReflectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @author cjbi
 */
public class DataOperationsGenerationDecorator extends AbstractDataOperationsDecorator {


  public DataOperationsGenerationDecorator(AbstractSessionContext sessionContext, DataOperations delegate) {
    super(sessionContext, delegate);
  }

  private Object convertParameter(TypedField<?, ?> field, Object value) {
    return sessionContext.getTypeHandlerMap().get(field.getType())
      .convertParameter(field, value);
  }

  public Map<String, Object> generateValue(String modelName, Map<String, Object> data, boolean isUpdate) {
    Entity entity = (Entity) sessionContext.getModel(modelName);
    List<TypedField<?, ?>> fields = entity.getFields();
    Map<String, Object> newData = new HashMap<>();
    // 类型转换
    data.forEach((key, value) -> {
      TypedField<?, ?> field = entity.getField(key);
      if (field != null && !(field instanceof RelationField)) {
        newData.put(field.getName(), convertParameter(field, value)
        );
      }
    });
    for (TypedField<?, ?> field : fields) {
      if (field instanceof RelationField) {
        continue;
      }
      Object value = newData.get(field.getName());
      // 仅支持新增修改默认值
      if (field.getDefaultValue() != null && newData.containsKey(field.getName())) {
        newData.put(field.getName(), generateValue(field, value, isUpdate));
      }
    }
    return newData;
  }

  private Object generateValue(TypedField<?, ?> field, Object value, boolean isUpdate) {
    if (!isUpdate && value == null) {
      if (Objects.equals(field.getDefaultValue(), GeneratedValue.ULID)) {
        return ULID.random().toString();
      } else if (Objects.equals(field.getDefaultValue(), GeneratedValue.UUID)) {
        return UUID.randomUUID().toString();
      } else if (field.getDefaultValue().equals(GeneratedValue.NOW)) {
        if (field instanceof DateTimeField) {
          return LocalDateTime.now();
        } else if (field instanceof DateField) {
          return LocalDate.now();
        } else if (field instanceof TimeField) {
          return LocalTime.now();
        }
      } else if (field.getDefaultValue() instanceof GeneratedValue) {
        // ignored
      } else {
        return convertParameter(field, field.getDefaultValue());
      }
    }
    return value;
  }

  @Override
  @SuppressWarnings("all")
  public int insert(String modelName, Object obj, Consumer<Object> idConsumer) {
    Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), obj, Map.class);
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    AtomicReference<Object> atomicId = new AtomicReference<>();
    SchemaObject model = mappedModels.getModel(schemaName, modelName);
    int rows = delegate.insert(modelName, generateValue(modelName, record, false), atomicId::set);
    Object id = atomicId.get();
    idConsumer.accept(id);

    insertRelationRecord(modelName, record, id);
    return rows;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void insertRelationRecord(String modelName, Object objR, Object id) {
    Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), objR, Map.class);
    Entity entity = (Entity) sessionContext.getModel(modelName);
    record.forEach((key, value) -> {
      if (value != null) {
        if (entity.getField(key) instanceof RelationField relationField) {
          if (relationField.isMultiple()) {
            java.util.Collection<?> collection = (java.util.Collection) value;
            for (Object obj : collection) {
              if (obj instanceof Map data) {
                Map<String, Object> associationRecord = new HashMap<>(data);
                associationRecord.put(relationField.getForeignField(), id);
                insert(relationField.getFrom(), associationRecord);
              }
            }
          } else {
            Map<String, Object> associationRecord = new HashMap<>((Map) value);
            associationRecord.put(relationField.getForeignField(), id);
            insert(relationField.getFrom(), associationRecord);
          }
        }
      }
    });
  }

  @Override
  public int update(String modelName, Object obj, String filter) {
    Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), obj, Map.class);
    return super.update(modelName, generateValue(modelName, record, true), filter);
  }

  @Override
  public int updateById(String modelName, Object obj, Object id) {
    Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), obj, Map.class);
    return super.updateById(modelName, generateValue(modelName, record, true), id);
  }
}
