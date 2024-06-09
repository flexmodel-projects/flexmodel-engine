package tech.wetech.flexmodel.generations;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.graph.JoinGraphNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static tech.wetech.flexmodel.RelationField.Cardinality.*;

/**
 * @author cjbi
 */
public class DataOperationsGenerationDecorator extends AbstractDataOperationsDecorator {

  private final DataValueGenerator dataValueGenerator;

  public DataOperationsGenerationDecorator(AbstractSessionContext sessionContext, DataOperations delegate) {
    super(sessionContext, delegate);
    this.dataValueGenerator = new DataValueGenerator(sessionContext);
  }

  @Override
  @SuppressWarnings("all")
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> idConsumer) {
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    AtomicReference<Object> atomicId = new AtomicReference<>();
    int rows = delegate.insert(modelName, dataValueGenerator.generateAll(modelName, record), atomicId::set);
    Object id = atomicId.get();
    idConsumer.accept(id);
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    record.forEach((key, value) -> {
      if (value != null) {
        if (entity.getField(key) instanceof RelationField relationField) {
          Entity targetEntity = mappedModels.getEntity(schemaName, relationField.getTargetEntity());
          if (relationField.getCardinality() == ONE_TO_ONE && value instanceof Map data) {
            Map<String, Object> associationRecord = new HashMap<>(data);
            associationRecord.put(relationField.getTargetField(), id);
            insert(relationField.getTargetEntity(), associationRecord);
          } else if (relationField.getCardinality() == ONE_TO_MANY && value instanceof Collection<?> collection) {
            for (Object obj : collection) {
              if (obj instanceof Map data) {
                Map<String, Object> associationRecord = new HashMap<>(data);
                associationRecord.put(relationField.getTargetField(), id);
                insert(relationField.getTargetEntity(), associationRecord);
              }
            }
          } else if (relationField.getCardinality() == MANY_TO_MANY && value instanceof Collection<?> collection) {
            for (Object obj : collection) {
              if (obj instanceof Map data) {
                Map<String, Object> associationRecord = new HashMap<>(data);
                AtomicReference<Object> inveseAtomicId = new AtomicReference<>();
                try {
                  insert(relationField.getTargetEntity(), associationRecord, inveseAtomicId::set);
                } catch (Exception e) {
                  if (data.containsKey(targetEntity.getIdField().getName())) {
                    inveseAtomicId.set(data.get(targetEntity.getIdField().getName()));
                  }
                }
                JoinGraphNode joinGraphNode = new JoinGraphNode(entity, targetEntity, relationField);
                associate(joinGraphNode, Map.of(
                  joinGraphNode.getJoinFieldName(), atomicId.get(),
                  joinGraphNode.getInverseJoinFieldName(), inveseAtomicId.get()
                ));
              }
            }
          }
        }
      }

    });
    return rows;
  }


  @Override
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    return delegate.updateById(modelName, dataValueGenerator.generate(modelName, record), id);
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    return delegate.update(modelName, dataValueGenerator.generate(modelName, record), filter);
  }

}
