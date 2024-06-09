package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class RelationField extends TypedField<Long, RelationField> {

  /**
   * 关联方式
   */
  private Cardinality cardinality = Cardinality.ONE_TO_MANY;
  /**
   * 级联删除，此功能依赖外键约束
   */
  private boolean cascadeDelete;
  /**
   * 目标实体
   */
  private String targetEntity;
  /**
   * 目标字段，如果存在则不创建外键字段，可不指定，不指定则为{entityName} + "Id"
   */
  private String targetField;

  public RelationField(String name) {
    super(name, BasicFieldType.RELATION.getType());
  }

  public boolean isCascadeDelete() {
    return cascadeDelete;
  }

  public Cardinality getCardinality() {
    return cardinality;
  }

  public RelationField setCascadeDelete(boolean cascadeDelete) {
    this.cascadeDelete = cascadeDelete;
    return this;
  }

  public RelationField setCardinality(Cardinality cardinality) {
    this.cardinality = cardinality;
    return this;
  }

  public String getTargetEntity() {
    return targetEntity;
  }

  public RelationField setTargetEntity(String targetEntity) {
    this.targetEntity = targetEntity;
    return this;
  }

  public String getTargetField() {
    return targetField;
  }

  public RelationField setTargetField(String targetField) {
    this.targetField = targetField;
    return this;
  }

  public enum Cardinality {
    ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY
  }

}
