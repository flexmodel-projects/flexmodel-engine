package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.graph.JoinGraphNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static tech.wetech.flexmodel.AssociationField.Cardinality.*;

/**
 * @author cjbi
 */
public class DataOperationsCalculationDecorator extends AbstractDataOperationsDecorator {

  private final DataCalculator dataCalculator;

  public DataOperationsCalculationDecorator(AbstractSessionContext sessionContext, DataOperations delegate) {
    super(sessionContext, delegate);
    this.dataCalculator = new DataCalculator(sessionContext);
  }

  @Override
  @SuppressWarnings("all")
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> idConsumer) {
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    AtomicReference<Object> atomicId = new AtomicReference<>();
    int rows = delegate.insert(modelName, dataCalculator.calculateAll(modelName, record), atomicId::set);
    Object id = atomicId.get();
    idConsumer.accept(id);
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    record.forEach((key, value) -> {
      if (value != null) {
        if (entity.getField(key) instanceof AssociationField associationField) {
          Entity targetEntity = mappedModels.getEntity(schemaName, associationField.getTargetEntity());
          if (associationField.getCardinality() == ONE_TO_ONE && value instanceof Map data) {
            Map<String, Object> associationRecord = new HashMap<>(data);
            associationRecord.put(associationField.getTargetField(), id);
            insert(associationField.getTargetEntity(), associationRecord);
          } else if (associationField.getCardinality() == ONE_TO_MANY && value instanceof Collection<?> collection) {
            for (Object obj : collection) {
              if (obj instanceof Map data) {
                Map<String, Object> associationRecord = new HashMap<>(data);
                associationRecord.put(associationField.getTargetField(), id);
                insert(associationField.getTargetEntity(), associationRecord);
              }
            }
          } else if (associationField.getCardinality() == MANY_TO_MANY && value instanceof Collection<?> collection) {
            for (Object obj : collection) {
              if (obj instanceof Map data) {
                Map<String, Object> associationRecord = new HashMap<>(data);
                AtomicReference<Object> inveseAtomicId = new AtomicReference<>();
                try {
                  insert(associationField.getTargetEntity(), associationRecord, inveseAtomicId::set);
                } catch (Exception e) {
                  if (data.containsKey(targetEntity.getIdField().getName())) {
                    inveseAtomicId.set(data.get(targetEntity.getIdField().getName()));
                  }
                }
                JoinGraphNode joinGraphNode = new JoinGraphNode(entity, targetEntity, associationField);
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
    return delegate.updateById(modelName, dataCalculator.calculate(modelName, record), id);
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    return delegate.update(modelName, dataCalculator.calculate(modelName, record), filter);
  }

}
