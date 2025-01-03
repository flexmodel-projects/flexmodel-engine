package tech.wetech.flexmodel.generator;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.graph.JoinGraphNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static tech.wetech.flexmodel.RelationField.Cardinality.*;

/**
 * @author cjbi
 */
public class DataOperationsGenerationDecorator extends AbstractDataOperationsDecorator {


  public DataOperationsGenerationDecorator(AbstractSessionContext sessionContext, DataOperations delegate) {
    super(sessionContext, delegate);
  }

  private Object convertParameter(TypedField<?, ?> field, Object value) {
    return sessionContext.getTypeHandlerMap().get(field instanceof IDField idField ? idField.getGeneratedValue().getType() : field.getType())
      .convertParameter(value);
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
    Model model = mappedModels.getModel(schemaName, modelName);
    int rows = delegate.insert(modelName, generateValue(modelName, record, false), atomicId::set);
    Object id = atomicId.get();
    idConsumer.accept(id);

    insertRelationRecord(modelName, record, id);
    return rows;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void insertRelationRecord(String modelName, Map<String, Object> record, Object id) {
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    record.forEach((key, value) -> {
      if (value != null) {
        if (entity.getField(key) instanceof RelationField relationField) {
          Entity targetEntity = (Entity) mappedModels.getModel(schemaName, relationField.getTargetEntity());
          switch (value) {
            case Map data when relationField.getCardinality() == ONE_TO_ONE -> {
              Map<String, Object> associationRecord = new HashMap<>(data);
              associationRecord.put(relationField.getTargetField(), id);
              insert(relationField.getTargetEntity(), associationRecord);
            }
            case Collection<?> collection when relationField.getCardinality() == ONE_TO_MANY -> {
              for (Object obj : collection) {
                if (obj instanceof Map data) {
                  Map<String, Object> associationRecord = new HashMap<>(data);
                  associationRecord.put(relationField.getTargetField(), id);
                  insert(relationField.getTargetEntity(), associationRecord);
                }
              }
            }
            case Collection<?> collection when relationField.getCardinality() == MANY_TO_MANY -> {
              for (Object obj : collection) {
                if (obj instanceof Map data) {
                  Map<String, Object> associationRecord = new HashMap<>(data);
                  AtomicReference<Object> inverseAtomicId = new AtomicReference<>();
                  try {
                    insert(relationField.getTargetEntity(), associationRecord, inverseAtomicId::set);
                  } catch (Exception e) {
                    if (data.containsKey(targetEntity.findIdField().map(IDField::getName).orElseThrow())) {
                      inverseAtomicId.set(data.get(targetEntity.findIdField().map(IDField::getName).orElseThrow()));
                    }
                  }
                  JoinGraphNode joinGraphNode = new JoinGraphNode(entity, targetEntity, relationField);
                  associate(joinGraphNode, Map.of(
                    joinGraphNode.getJoinFieldName(), id,
                    joinGraphNode.getInverseJoinFieldName(), inverseAtomicId.get()
                  ));
                }
              }
            }
            default -> {
            }
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
