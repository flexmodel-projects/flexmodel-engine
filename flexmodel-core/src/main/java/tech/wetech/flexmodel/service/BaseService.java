package tech.wetech.flexmodel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.generator.ULID;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.field.*;
import tech.wetech.flexmodel.reflect.ReflectionUtils;
import tech.wetech.flexmodel.session.AbstractSessionContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author cjbi
 */
public abstract class BaseService {

  private static final Logger log = LoggerFactory.getLogger(BaseService.class);

  private final AbstractSessionContext sessionContext;

  public BaseService(AbstractSessionContext sessionContext) {
    this.sessionContext = sessionContext;
  }

  /**
   * 数据操作实现类
   * @return
   */
  public DataService getDataService() {
    return null;
  }

  /**
   * 生成字段值（内联了DataOperationsGenerationDecorator的逻辑）
   */
  protected Map<String, Object> generateValue(String modelName, Map<String, Object> data, boolean isUpdate) {
    EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
    List<TypedField<?, ?>> fields = entity.getFields();
    Map<String, Object> newData = new HashMap<>();

    // 类型转换
    data.forEach((key, value) -> {
      TypedField<?, ?> field = entity.getField(key);
      if (field != null && !(field instanceof RelationField)) {
        newData.put(field.getName(), convertParameter(field, value));
      }
    });

    // 处理默认值和生成值
    for (TypedField<?, ?> field : fields) {
      if (field instanceof RelationField) {
        continue;
      }
      Object value = newData.get(field.getName());
      if (field.getDefaultValue() != null) {
        // 修复：对于有默认值的字段，无论是否在输入数据中存在，都应该生成默认值
        newData.put(field.getName(), generateFieldValue(field, value, isUpdate));
      }
    }

    return newData;
  }

  protected Object convertParameter(TypedField<?, ?> field, Object value) {
    return sessionContext.getTypeHandlerMap().get(field.getType())
      .convertParameter(field, value);
  }

  protected Object generateFieldValue(TypedField<?, ?> field, Object value, boolean isUpdate) {
    if (value == null) {
      if (Objects.equals(field.getDefaultValue(), GeneratedValue.ULID)) {
        return ULID.random().toString();
      } else if (Objects.equals(field.getDefaultValue(), GeneratedValue.UUID)) {
        return UUID.randomUUID().toString();
      } else if (field.getDefaultValue().equals(GeneratedValue.NOW)) {
        if (field instanceof DateTimeField) {
          return LocalDateTime.now();
        } else if (field instanceof DateField) {
          return LocalDate.now();
        } else if (field instanceof TimeField) {
          return LocalTime.now();
        }
      } else if (field.getDefaultValue() instanceof GeneratedValue) {
        // 忽略其他生成值
      } else {
        return convertParameter(field, field.getDefaultValue());
      }
    }
    return value;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  protected void insertRelationRecord(String modelName, Object objR, Object id) {
    Map<String, Object> record = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), objR, Map.class);
    EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);

    record.forEach((key, value) -> {
      if (value != null) {
        if (entity.getField(key) instanceof RelationField relationField) {
          if (relationField.isMultiple()) {
            Collection<?> collection = (Collection) value;
            collection.forEach(item -> {
              Map<String, Object> relationRecord = ReflectionUtils.toClassBean(
                sessionContext.getJsonObjectConverter(), item, Map.class);
              relationRecord.put(relationField.getForeignField(), id);
              getDataService().insert(relationField.getFrom(), relationRecord);
            });
          } else {
            Map<String, Object> relationRecord = ReflectionUtils.toClassBean(
              sessionContext.getJsonObjectConverter(), value, Map.class);
            relationRecord.put(relationField.getForeignField(), id);
            getDataService().insert(relationField.getFrom(), relationRecord);
          }
        }
      }
    });
  }

  /**
   * 故障安全模式检查（内联了SchemaOperationsPersistenceDecorator的逻辑）
   */
  protected <T> T inspect(Supplier<T> supplier, T orElse) {
    try {
      return supplier.get();
    } catch (Exception e) {
      if (!sessionContext.isFailsafe()) {
        throw e;
      }
      log.warn("Schema error: {}", e.getMessage());
      return orElse;
    }
  }

  protected void inspect(Runnable runnable) {
    try {
      runnable.run();
    } catch (Exception e) {
      if (!sessionContext.isFailsafe()) {
        throw e;
      }
      log.warn("Schema error: {}", e.getMessage());
    }
  }

}
