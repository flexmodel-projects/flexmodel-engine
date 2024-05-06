package tech.wetech.flexmodel;

/**
 * @author cjbi
 */
public class AssociationField extends TypedField<Long, AssociationField> {

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

  public AssociationField(String name) {
    super(name, BasicFieldType.ASSOCIATION.getType());
  }

  public boolean cascadeDelete() {
    return cascadeDelete;
  }

  public Cardinality cardinality() {
    return cardinality;
  }

  public AssociationField setCascadeDelete(boolean cascadeDelete) {
    this.cascadeDelete = cascadeDelete;
    return this;
  }

  public AssociationField setCardinality(Cardinality cardinality) {
    this.cardinality = cardinality;
    return this;
  }

  public String targetEntity() {
    return targetEntity;
  }

  public AssociationField setTargetEntity(String targetEntity) {
    this.targetEntity = targetEntity;
    return this;
  }

  public String targetField() {
    if (targetField == null) {
      return modelName() + "_id";
    }
    return targetField;
  }

  public AssociationField setTargetField(String targetField) {
    this.targetField = targetField;
    return this;
  }

  public enum Cardinality {
    ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY
  }

}
