package tech.wetech.flexmodel.query;

import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.TypedField;
import tech.wetech.flexmodel.reflect.ReflectionUtils;
import tech.wetech.flexmodel.session.Session;

import java.util.Map;
import java.util.Optional;

/**
 * DSL插入构建器
 */
public class DSLMergeBuilder {
  private final Session session;
  private String modelName;
  private Class<?> entityClass;
  private Map<String, Object> values;

  public DSLMergeBuilder(Session session) {
    this.session = session;
  }

  /**
   * 设置要插入的模型名称
   */
  public DSLMergeBuilder mergeInto(String modelName) {
    this.modelName = modelName;
    this.entityClass = null;
    return this;
  }

  /**
   * 设置要插入的实体类
   */
  public <T> TypedDSLMergeBuilder<T> mergeInto(Class<T> entityClass) {
    this.entityClass = entityClass;
    this.modelName = ReflectionUtils.getModelNameFromClass(entityClass);
    return new TypedDSLMergeBuilder<>(this, entityClass);
  }

  /**
   * 设置要插入的值
   */
  public DSLMergeBuilder values(Map<String, Object> values) {
    this.values = values;
    return this;
  }

  /**
   * 执行插入操作
   */
  public int execute() {
    if (modelName == null) {
      throw new IllegalStateException("Model name or entity class must be specified using mergeInto() method");
    }
    if (values == null) {
      throw new IllegalStateException("Values must be specified using values() method");
    }
    EntityDefinition entity = (EntityDefinition) session.schema().getModel(modelName);
    Optional<TypedField<?, ?>> idFieldOptional = entity.findIdField();
    if (idFieldOptional.isPresent()) {
      Object id = values.get(idFieldOptional.get().getName());
      if (id == null) {
        return session.data().insert(modelName, values);
      }
      int rows = session.data().updateById(modelName, values, id);
      if (rows != 0) {
        return rows;
      } else {
        return session.data().insert(modelName, values);
      }
    } else {
      return session.data().insert(modelName, values);
    }
  }

}
