package tech.wetech.flexmodel.core.model.field;

import java.util.Objects;

/**
 * @author cjbi
 */
public class RelationField extends TypedField<Long, RelationField> {

  /**
   * 多选
   */
  private boolean multiple;
  /**
   * 目标实体
   */
  private String from;
  /**
   * 本地字段
   */
  private String localField;
  /**
   * 外键字段
   */
  private String foreignField;
  /**
   * 级联删除，此功能依赖外键约束
   */
  private boolean cascadeDelete;

  public RelationField(String name) {
    super(name, ScalarType.RELATION.getType());
  }

  public boolean isCascadeDelete() {
    return cascadeDelete;
  }

  public RelationField setCascadeDelete(boolean cascadeDelete) {
    this.cascadeDelete = cascadeDelete;
    return this;
  }

  public String getFrom() {
    return from;
  }

  public RelationField setFrom(String from) {
    this.from = from;
    return this;
  }

  public String getLocalField() {
    return localField;
  }

  public RelationField setLocalField(String localField) {
    this.localField = localField;
    return this;
  }

  public String getForeignField() {
    return foreignField;
  }

  public RelationField setForeignField(String foreignField) {
    this.foreignField = foreignField;
    return this;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public RelationField setMultiple(boolean multiple) {
    this.multiple = multiple;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RelationField that)) return false;
    if (!super.equals(o)) return false;
    return isMultiple() == that.isMultiple() &&
           isCascadeDelete() == that.isCascadeDelete() &&
           Objects.equals(getFrom(), that.getFrom()) &&
           Objects.equals(getForeignField(), that.getForeignField());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), isMultiple(), getFrom(), getForeignField(), isCascadeDelete());
  }

  @Override
  public String getConcreteType() {
    return from + (multiple ? "[]" : "");
  }
}
