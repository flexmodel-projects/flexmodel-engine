package tech.wetech.flexmodel.graph;

import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.IDField;
import tech.wetech.flexmodel.RelationField;
import tech.wetech.flexmodel.TypedField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author cjbi
 */
public class JoinGraphNode {

  private final String joinName;
  private final String joinFieldName;
  private final String joinFieldType;
  private final String inverseJoinFieldName;
  private final String inverseJoinFieldType;

  public JoinGraphNode(Entity entity, Entity targetEntity, RelationField relationField) {
    List<String> modelNames = new ArrayList<>();
    modelNames.add(entity.getName());
    modelNames.add(relationField.getTargetEntity());
    Collections.sort(modelNames);
    this.joinName = String.join("_", modelNames);
    this.joinFieldName = entity.getName() + "_" + entity.getIdField().getName();
    this.joinFieldType = entity.getIdField().getGeneratedValue().getType();
    this.inverseJoinFieldName = relationField.getTargetEntity() + "_" + relationField.getTargetField();

    TypedField<?, ?> targetField = (TypedField<?, ?>) targetEntity.getField(relationField.getTargetField());
    this.inverseJoinFieldType = targetField instanceof IDField idField
      ? idField.getGeneratedValue().getType()
      : targetField.getType();
  }

  public String getJoinName() {
    return joinName;
  }

  public String getJoinFieldName() {
    return joinFieldName;
  }

  public String getJoinFieldType() {
    return joinFieldType;
  }

  public String getInverseJoinFieldName() {
    return inverseJoinFieldName;
  }

  public String getInverseJoinFieldType() {
    return inverseJoinFieldType;
  }
}
