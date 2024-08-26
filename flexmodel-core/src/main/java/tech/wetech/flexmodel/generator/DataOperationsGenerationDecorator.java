package tech.wetech.flexmodel.generator;

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

  private final DataValueGeneratorFacade dataValueGeneratorFacade;

  public DataOperationsGenerationDecorator(AbstractSessionContext sessionContext, DataOperations delegate) {
    super(sessionContext, delegate);
    this.dataValueGeneratorFacade = new DataValueGeneratorFacade(sessionContext);
  }

  @Override
  @SuppressWarnings("all")
  public int insert(String modelName, Map<String, Object> record, Consumer<Object> idConsumer) {
    String schemaName = sessionContext.getSchemaName();
    MappedModels mappedModels = sessionContext.getMappedModels();
    AtomicReference<Object> atomicId = new AtomicReference<>();
    int rows = delegate.insert(modelName, dataValueGeneratorFacade.generateValue(modelName, record, false), atomicId::set);
    Object id = atomicId.get();
    idConsumer.accept(id);
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    insertRelationRecord(modelName, record, id);
    return rows;
  }

  @SuppressWarnings({"rawtypes","unchecked"})
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
  public int updateById(String modelName, Map<String, Object> record, Object id) {
    return delegate.updateById(modelName, dataValueGeneratorFacade.generateValue(modelName, record, true), id);
  }

  @Override
  public int update(String modelName, Map<String, Object> record, String filter) {
    return delegate.update(modelName, dataValueGeneratorFacade.generateValue(modelName, record, true), filter);
  }

}
