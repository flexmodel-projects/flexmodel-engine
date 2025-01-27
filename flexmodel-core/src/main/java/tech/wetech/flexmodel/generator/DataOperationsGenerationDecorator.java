package tech.wetech.flexmodel.generator;

import tech.wetech.flexmodel.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    return sessionContext.getTypeHandlerMap().get(field instanceof IDField idField ? idField.getGeneratedValue().getType() : field.getType())
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
      Object value = data.get(field.getName());
      // 仅支持新增修改默认值
      if (value == null && !isUpdate) {
        if (field instanceof IDField idField) {
          if (idField.getGeneratedValue() == IDField.GeneratedValue.ULID) {
            newData.put(field.getName(), ULID.random().toString());
          } else if (idField.getGeneratedValue() == IDField.GeneratedValue.UUID) {
            newData.put(field.getName(), UUID.randomUUID().toString());
          }
        } else if (field.getDefaultValue() != null) {
          newData.put(field.getName(), field.getDefaultValue());
        }
      }
    }
    return newData;
  }

  @Override
  @SuppressWarnings("all")
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> idConsumer) {
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    AtomicReference<Object> atomicId = new AtomicReference<>();
    TypeWrapper model = mappedModels.getModel(schemaName, modelName);
    int rows = delegate.insert(modelName, generateValue(modelName, record, false), atomicId::set);
    Object id = atomicId.get();
    idConsumer.accept(id);

    insertRelationRecord(modelName, record, id);
    return rows;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void insertRelationRecord(String modelName, Map<String, Object> record, Object id) {
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
  public int update(String modelName, Map<String, Object> record, String filter) {
    return super.update(modelName, generateValue(modelName, record, true), filter);
  }

  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    return super.updateById(modelName, generateValue(modelName, record, true), id);
  }
}
