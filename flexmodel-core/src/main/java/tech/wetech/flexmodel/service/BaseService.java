package tech.wetech.flexmodel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.generator.ULID;
import tech.wetech.flexmodel.model.EntityDefinition;
import tech.wetech.flexmodel.model.ModelDefinition;
import tech.wetech.flexmodel.model.field.*;
import tech.wetech.flexmodel.query.Query;
import tech.wetech.flexmodel.reflect.ReflectionUtils;
import tech.wetech.flexmodel.session.AbstractSessionContext;
import tech.wetech.flexmodel.sql.SqlExecutionException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static tech.wetech.flexmodel.query.expr.Expressions.field;

/**
 * 基础服务类，提供查询验证、关联字段处理、嵌套查询、字段值生成等核心功能
 *
 * @author cjbi
 */
public abstract class BaseService {

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private final AbstractSessionContext sessionContext;

  public BaseService(AbstractSessionContext sessionContext) {
    this.sessionContext = sessionContext;
    log.debug("BaseService initialized with sessionContext: {}", sessionContext.getClass().getSimpleName());
  }

  /**
   * 获取数据操作实现类
   *
   * @return 数据服务实例，子类需要重写此方法
   */
  public DataService getDataService() {
    log.debug("getDataService() called, returning null - should be overridden by subclass");
    return null;
  }

  /**
   * 验证查询的合法性
   * 包括：分组字段验证、连接字段验证等
   *
   * @param modelName 模型名称
   * @param query 查询对象
   * @throws RuntimeException 当查询验证失败时抛出
   * @throws SqlExecutionException 当连接配置错误时抛出
   */
  public void validateQuery(String modelName, Query query) {
    log.debug("Starting query validation for model: {}, query: {}", modelName, query);
    
    try {
      validateGroupByFields(query);
      validateJoinFields(modelName, query);
      log.debug("Query validation completed successfully for model: {}", modelName);
    } catch (Exception e) {
      log.error("Query validation failed for model: {}, error: {}", modelName, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 验证分组字段的合法性
   * 确保所有非聚合字段都在GROUP BY子句中
   *
   * @param query 查询对象
   * @throws RuntimeException 当分组字段验证失败时抛出
   */
  private void validateGroupByFields(Query query) {
    Query.Projection projection = query.getProjection();
    Query.GroupBy groupBy = query.getGroupBy();

    if (groupBy == null) {
      log.debug("No GROUP BY clause found, skipping group validation");
      return;
    }

    log.debug("Validating GROUP BY fields, groupBy: {}", groupBy);

    Set<String> groupedFieldNames = groupBy.getFields().stream()
      .map(Query.QueryField::getName)
      .collect(Collectors.toSet());

    log.debug("Grouped field names: {}", groupedFieldNames);

    List<String> ungroupedFieldNames = new ArrayList<>();

    if (projection != null) {
      Map<String, Query.QueryCall> projectionFields = projection.getFields();
      log.debug("Validating {} projection fields", projectionFields.size());
      
      for (Map.Entry<String, Query.QueryCall> entry : projectionFields.entrySet()) {
        String fieldAlias = entry.getKey();
        Query.QueryCall fieldExpression = entry.getValue();

        if (fieldExpression instanceof Query.QueryField queryField) {
          if (!groupedFieldNames.contains(fieldAlias) && !groupedFieldNames.contains(queryField.getName())) {
            ungroupedFieldNames.add(fieldAlias);
            log.warn("Ungrouped field found: {} (alias: {})", queryField.getName(), fieldAlias);
          }
        }
      }
    } else {
      log.error("GROUP BY validation failed: projection is null");
      throw new RuntimeException("Group by error: projection is null");
    }

    if (!ungroupedFieldNames.isEmpty()) {
      String errorMessage = "The fields " + String.join(", ", ungroupedFieldNames) + " has not been grouped or aggregated";
      log.error("GROUP BY validation failed: {}", errorMessage);
      throw new RuntimeException(errorMessage);
    }

    log.debug("GROUP BY validation completed successfully");
  }

  /**
   * 验证连接字段的合法性
   * 包括：连接模型验证、关联字段自动填充等
   *
   * @param modelName 主模型名称
   * @param query 查询对象
   * @throws SqlExecutionException 当连接配置错误时抛出
   */
  private void validateJoinFields(String modelName, Query query) {
    if (query.getJoins() == null) {
      log.debug("No JOIN clauses found, skipping join validation");
      return;
    }

    log.debug("Validating JOIN fields for model: {}, joins count: {}", modelName, query.getJoins().getJoins().size());

    ModelDefinition mainModel = (ModelDefinition) sessionContext.getModel(modelName);

    for (Query.Join joinConfig : query.getJoins().getJoins()) {
      log.debug("Validating join config: from={}, as={}, localField={}, foreignField={}", 
                joinConfig.getFrom(), joinConfig.getAs(), joinConfig.getLocalField(), joinConfig.getForeignField());
      validateJoinConfiguration(joinConfig, mainModel);
    }

    log.debug("JOIN validation completed successfully");
  }

  /**
   * 验证单个连接配置
   *
   * @param joinConfig 连接配置
   * @param mainModel 主模型定义
   * @throws SqlExecutionException 当连接配置错误时抛出
   */
  private void validateJoinConfiguration(Query.Join joinConfig, ModelDefinition mainModel) {
    if (joinConfig.getFrom() == null) {
      log.error("Join validation failed: from model is null");
      throw new SqlExecutionException("Join from model must not be null");
    }

    if (mainModel instanceof EntityDefinition entity &&
        joinConfig.getLocalField() == null &&
        joinConfig.getForeignField() == null) {
      // 自动填充关联字段
      log.debug("Auto-filling relation fields for join: {}", joinConfig.getFrom());
      autoFillRelationFields(joinConfig, entity);
    } else {
      // 验证手动配置的连接字段
      log.debug("Validating manual join fields");
      validateManualJoinFields(joinConfig);
    }

    // 添加别名模型映射
    sessionContext.addAliasModelIfPresent(
      joinConfig.getAs(),
      (ModelDefinition) sessionContext.getModel(joinConfig.getFrom())
    );
    
    log.debug("Join configuration validated successfully: {}", joinConfig.getFrom());
  }

  /**
   * 自动填充关联字段
   *
   * @param joinConfig 连接配置
   * @param entity 实体定义
   */
  private void autoFillRelationFields(Query.Join joinConfig, EntityDefinition entity) {
    try {
      RelationField relationField = entity.findRelationByModelName(joinConfig.getFrom())
        .orElseThrow(() -> new SqlExecutionException("Relation field not found for model: " + joinConfig.getFrom()));

      String localFieldName = relationField.getLocalField() != null ?
        relationField.getLocalField() :
        entity.findIdField().map(TypedField::getName).orElseThrow();

      joinConfig.setLocalField(localFieldName);
      joinConfig.setForeignField(relationField.getForeignField());

      log.debug("Auto-filled relation fields: localField={}, foreignField={}", 
                localFieldName, relationField.getForeignField());
    } catch (Exception e) {
      log.error("Failed to auto-fill relation fields for join: {}, error: {}", joinConfig.getFrom(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 验证手动配置的连接字段
   *
   * @param joinConfig 连接配置
   * @throws SqlExecutionException 当字段配置错误时抛出
   */
  private void validateManualJoinFields(Query.Join joinConfig) {
    if (joinConfig.getLocalField() == null || joinConfig.getForeignField() == null) {
      log.error("Manual join validation failed: localField={}, foreignField={}", 
                joinConfig.getLocalField(), joinConfig.getForeignField());
      throw new SqlExecutionException("LocalField and foreignField must not be null when is not association field");
    }
  }

  /**
   * 查找查询中的关联字段
   *
   * @param model 模型定义
   * @param query 查询对象
   * @return 关联字段映射，key为字段别名，value为关联字段定义
   */
  public Map<String, RelationField> findRelationFields(ModelDefinition model, Query query) {
    log.debug("Finding relation fields for model: {}, hasProjection: {}", 
              model.getName(), hasProjectionFields(query));

    Map<String, RelationField> relationFieldMap = new HashMap<>();

    if (!(model instanceof EntityDefinition entity)) {
      log.debug("Model is not EntityDefinition, returning empty relation field map");
      return relationFieldMap;
    }

    if (hasProjectionFields(query)) {
      findRelationFieldsFromProjection(entity, query, relationFieldMap);
    } else {
      findAllRelationFields(entity, relationFieldMap);
    }

    log.debug("Found {} relation fields: {}", relationFieldMap.size(), relationFieldMap.keySet());
    return relationFieldMap;
  }

  /**
   * 检查查询是否有投影字段
   *
   * @param query 查询对象
   * @return 是否有投影字段
   */
  private boolean hasProjectionFields(Query query) {
    return query != null &&
           query.getProjection() != null &&
           !query.getProjection().getFields().isEmpty();
  }

  /**
   * 从投影字段中查找关联字段
   *
   * @param entity 实体定义
   * @param query 查询对象
   * @param relationFieldMap 关联字段映射
   */
  private void findRelationFieldsFromProjection(EntityDefinition entity, Query query, Map<String, RelationField> relationFieldMap) {
    log.debug("Finding relation fields from projection, projection fields count: {}", 
              query.getProjection().getFields().size());

    for (Map.Entry<String, Query.QueryCall> entry : query.getProjection().getFields().entrySet()) {
      String fieldAlias = entry.getKey();
      Query.QueryCall fieldExpression = entry.getValue();

      if (fieldExpression instanceof Query.QueryField queryField) {
        log.debug("Checking field: {} (alias: {})", queryField.getName(), fieldAlias);
        
        entity.getFields().stream()
          .filter(field -> field.getName().equals(queryField.getName()) && field instanceof RelationField)
          .map(field -> (RelationField) field)
          .findFirst()
          .ifPresent(relationField -> {
            relationFieldMap.put(fieldAlias, relationField);
            log.debug("Found relation field: {} -> {}", fieldAlias, relationField.getName());
          });
      }
    }
  }

  /**
   * 查找所有关联字段
   *
   * @param entity 实体定义
   * @param relationFieldMap 关联字段映射
   */
  private void findAllRelationFields(EntityDefinition entity, Map<String, RelationField> relationFieldMap) {
    log.debug("Finding all relation fields for entity: {}", entity.getName());

    for (Field field : entity.getFields()) {
      if (field instanceof RelationField relationField) {
        relationFieldMap.put(relationField.getName(), relationField);
        log.debug("Found relation field: {}", relationField.getName());
      }
    }
  }

  /**
   * 查找关联数据列表
   *
   * @param relationQueryFunction 关联查询函数
   * @param relationField 关联字段定义
   * @param foreignKeyValues 外键值集合
   * @return 关联数据列表
   */
  private List<Map<String, Object>> findRelationDataList(
    BiFunction<String, Query, List<Map<String, Object>>> relationQueryFunction,
    RelationField relationField,
    Set<Object> foreignKeyValues) {
    
    log.debug("Finding relation data for field: {}, foreign key values count: {}", 
              relationField.getName(), foreignKeyValues.size());

    Query relationQuery = new Query();
    relationQuery.setFilter(field(relationField.getForeignField()).in(foreignKeyValues).toJsonString());
    
    List<Map<String, Object>> result = relationQueryFunction.apply(relationField.getFrom(), relationQuery);
    
    log.debug("Found {} relation data records for field: {}", result.size(), relationField.getName());
    return result;
  }

  /**
   * 执行嵌套查询
   *
   * @param parentDataList 父级数据列表
   * @param relationQueryFunction 关联查询函数
   * @param model 模型定义
   * @param query 查询对象
   * @param maxDepth 最大嵌套深度
   */
  public void nestedQuery(List<Map<String, Object>> parentDataList,
                          BiFunction<String, Query, List<Map<String, Object>>> relationQueryFunction,
                          ModelDefinition model,
                          Query query,
                          int maxDepth) {
    log.debug("Starting nested query for model: {}, parent data count: {}, max depth: {}", 
              model.getName(), parentDataList.size(), maxDepth);

    try {
      nestedQuery(parentDataList, relationQueryFunction, model, query, new AtomicInteger(maxDepth));
      log.debug("Nested query completed successfully for model: {}", model.getName());
    } catch (Exception e) {
      log.error("Nested query failed for model: {}, error: {}", model.getName(), e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 执行嵌套查询（私有方法，使用原子整数控制深度）
   *
   * @param parentDataList 父级数据列表
   * @param relationQueryFunction 关联查询函数
   * @param model 模型定义
   * @param query 查询对象
   * @param remainingDepth 剩余深度（原子整数）
   */
  private void nestedQuery(List<Map<String, Object>> parentDataList,
                           BiFunction<String, Query, List<Map<String, Object>>> relationQueryFunction,
                           ModelDefinition model,
                           Query query,
                           AtomicInteger remainingDepth) {
    if (remainingDepth.get() <= 0) {
      log.debug("Nested query depth limit reached for model: {}", model.getName());
      return;
    }

    log.debug("Processing nested query for model: {}, remaining depth: {}", model.getName(), remainingDepth.get());

    Map<String, RelationField> relationFieldMap = findRelationFields(model, query);

    relationFieldMap.entrySet().parallelStream().forEach(entry -> {
      String relationFieldAlias = entry.getKey();
      RelationField relationField = entry.getValue();

      log.debug("Processing relation field: {} (alias: {})", relationField.getName(), relationFieldAlias);
      processRelationField(parentDataList, relationQueryFunction, model, query,
        remainingDepth, relationFieldAlias, relationField);
    });
  }

  /**
   * 处理单个关联字段
   *
   * @param parentDataList 父级数据列表
   * @param relationQueryFunction 关联查询函数
   * @param model 模型定义
   * @param query 查询对象
   * @param remainingDepth 剩余深度
   * @param relationFieldAlias 关联字段别名
   * @param relationField 关联字段定义
   */
  private void processRelationField(List<Map<String, Object>> parentDataList,
                                   BiFunction<String, Query, List<Map<String, Object>>> relationQueryFunction,
                                   ModelDefinition model,
                                   Query query,
                                   AtomicInteger remainingDepth,
                                   String relationFieldAlias,
                                   RelationField relationField) {
    // 收集所有外键值
    Set<Object> foreignKeyValues = parentDataList.stream()
      .map(dataItem -> dataItem.get(relationField.getLocalField()))
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());

    log.debug("Collected {} foreign key values for relation field: {}", 
              foreignKeyValues.size(), relationField.getName());

    EntityDefinition relationModel = (EntityDefinition) sessionContext.getModel(relationField.getFrom());

    if (relationModel == null || relationModel.getField(relationField.getForeignField()) == null) {
      log.warn("Relation model or foreign field not found for relation: {}", relationField.getName());
      return;
    }

    // 查询关联数据并按外键分组
    Map<Object, List<Map<String, Object>>> relationDataGroup = findRelationDataList(relationQueryFunction, relationField, foreignKeyValues)
      .stream()
      .collect(Collectors.groupingBy(dataItem -> dataItem.get(relationField.getForeignField())));

    log.debug("Grouped relation data by foreign key, groups count: {}", relationDataGroup.size());

    // 填充关联数据到父级数据中
    parentDataList.forEach(parentDataItem -> {
      if (model instanceof EntityDefinition) {
        fillRelationDataToParent(parentDataItem, relationField, relationFieldAlias,
          relationDataGroup, relationQueryFunction, remainingDepth, relationModel);
      }
    });
  }

  /**
   * 将关联数据填充到父级数据中
   *
   * @param parentDataItem 父级数据项
   * @param relationField 关联字段定义
   * @param relationFieldAlias 关联字段别名
   * @param relationDataGroup 关联数据分组
   * @param relationQueryFunction 关联查询函数
   * @param remainingDepth 剩余深度
   * @param relationModel 关联模型定义
   */
  private void fillRelationDataToParent(Map<String, Object> parentDataItem,
                                       RelationField relationField,
                                       String relationFieldAlias,
                                       Map<Object, List<Map<String, Object>>> relationDataGroup,
                                       BiFunction<String, Query, List<Map<String, Object>>> relationQueryFunction,
                                       AtomicInteger remainingDepth,
                                       EntityDefinition relationModel) {
    Object localKeyValue = parentDataItem.get(relationField.getLocalField());

    if (localKeyValue == null) {
      log.debug("Local key value is null for relation field: {}", relationField.getName());
      parentDataItem.put(relationFieldAlias, relationField.isMultiple() ? List.of() : null);
      return;
    }

    List<Map<String, Object>> relationDataList = relationDataGroup.getOrDefault(localKeyValue, List.of());

    log.debug("Found {} relation data records for local key: {}", relationDataList.size(), localKeyValue);

    // 递归处理嵌套查询
    remainingDepth.decrementAndGet();
    nestedQuery(relationDataList, relationQueryFunction, relationModel, null, remainingDepth);

    // 根据关联类型设置值
    Object relationValue = relationField.isMultiple() ?
      relationDataList :
      (!relationDataList.isEmpty() ? relationDataList.getFirst() : null);

    parentDataItem.put(relationFieldAlias, relationValue);
    
    log.debug("Set relation value for field: {} (alias: {}), value type: {}", 
              relationField.getName(), relationFieldAlias, 
              relationValue != null ? relationValue.getClass().getSimpleName() : "null");
  }

  /**
   * 生成字段值，包括类型转换、默认值处理、自动生成值等
   *
   * @param modelName 模型名称
   * @param inputData 输入数据
   * @param isUpdate 是否为更新操作
   * @return 处理后的数据
   */
  protected Map<String, Object> generateValue(String modelName, Map<String, Object> inputData, boolean isUpdate) {
    log.debug("Generating field values for model: {}, input data size: {}, isUpdate: {}", 
              modelName, inputData.size(), isUpdate);

    EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
    List<TypedField<?, ?>> entityFields = entity.getFields();
    Map<String, Object> processedData = new HashMap<>();

    // 处理输入数据的类型转换
    processInputData(inputData, entity, processedData);

    // 处理默认值和自动生成值
    processDefaultAndGeneratedValues(entityFields, processedData, isUpdate);

    log.debug("Field value generation completed for model: {}, processed data size: {}", 
              modelName, processedData.size());
    return processedData;
  }

  /**
   * 处理输入数据的类型转换
   *
   * @param inputData 输入数据
   * @param entity 实体定义
   * @param processedData 处理后的数据
   */
  private void processInputData(Map<String, Object> inputData, EntityDefinition entity, Map<String, Object> processedData) {
    log.debug("Processing input data for entity: {}, input fields: {}", entity.getName(), inputData.keySet());

    inputData.forEach((fieldName, fieldValue) -> {
      TypedField<?, ?> field = entity.getField(fieldName);
      if (field != null && !(field instanceof RelationField)) {
        Object convertedValue = convertParameter(field, fieldValue);
        processedData.put(field.getName(), convertedValue);
        log.debug("Converted field: {} = {} -> {}", fieldName, fieldValue, convertedValue);
      } else {
        log.debug("Skipped field: {} (not found or is relation field)", fieldName);
      }
    });
  }

  /**
   * 处理默认值和自动生成值
   *
   * @param entityFields 实体字段列表
   * @param processedData 处理后的数据
   * @param isUpdate 是否为更新操作
   */
  private void processDefaultAndGeneratedValues(List<TypedField<?, ?>> entityFields,
                                               Map<String, Object> processedData,
                                               boolean isUpdate) {
    log.debug("Processing default and generated values for {} fields", entityFields.size());

    for (TypedField<?, ?> field : entityFields) {
      if (field instanceof RelationField) {
        continue;
      }

      Object currentValue = processedData.get(field.getName());
      if (field.getDefaultValue() != null) {
        Object generatedValue = generateFieldValue(field, currentValue, isUpdate);
        processedData.put(field.getName(), generatedValue);
        
        if (!Objects.equals(currentValue, generatedValue)) {
          log.debug("Generated value for field: {} = {} -> {}", 
                    field.getName(), currentValue, generatedValue);
        }
      }
    }
  }

  /**
   * 转换参数类型
   *
   * @param field 字段定义
   * @param value 原始值
   * @return 转换后的值
   */
  protected Object convertParameter(TypedField<?, ?> field, Object value) {
    try {
      Object convertedValue = sessionContext.getTypeHandlerMap().get(field.getType())
      .convertParameter(field, value);
      
      log.debug("Converted parameter: field={}, type={}, value={} -> {}", 
                field.getName(), field.getType(), value, convertedValue);
      
      return convertedValue;
    } catch (Exception e) {
      log.error("Failed to convert parameter: field={}, type={}, value={}, error: {}", 
                field.getName(), field.getType(), value, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 生成字段值，包括自动生成ID、时间戳等
   *
   * @param field 字段定义
   * @param currentValue 当前值
   * @param isUpdate 是否为更新操作
   * @return 生成的值
   */
  protected Object generateFieldValue(TypedField<?, ?> field, Object currentValue, boolean isUpdate) {
    if (currentValue != null) {
      return currentValue;
    }

    Object defaultValue = field.getDefaultValue();

    if (Objects.equals(defaultValue, GeneratedValue.ULID)) {
      String ulid = ULID.random().toString();
      log.debug("Generated ULID for field: {} = {}", field.getName(), ulid);
      return ulid;
    } else if (Objects.equals(defaultValue, GeneratedValue.UUID)) {
      String uuid = UUID.randomUUID().toString();
      log.debug("Generated UUID for field: {} = {}", field.getName(), uuid);
      return uuid;
    } else if (Objects.equals(defaultValue, GeneratedValue.NOW)) {
      Object timeValue = generateCurrentTimeValue(field);
      log.debug("Generated current time for field: {} = {}", field.getName(), timeValue);
      return timeValue;
    } else if (defaultValue instanceof GeneratedValue) {
      // 忽略其他生成值类型
      log.debug("Ignored generated value type for field: {} = {}", field.getName(), defaultValue);
      return null;
    } else {
      Object convertedDefault = convertParameter(field, defaultValue);
      log.debug("Used default value for field: {} = {}", field.getName(), convertedDefault);
      return convertedDefault;
    }
  }

  /**
   * 生成当前时间值
   *
   * @param field 字段定义
   * @return 当前时间值
   */
  private Object generateCurrentTimeValue(TypedField<?, ?> field) {
        if (field instanceof DateTimeField) {
          return LocalDateTime.now();
        } else if (field instanceof DateField) {
          return LocalDate.now();
        } else if (field instanceof TimeField) {
          return LocalTime.now();
        }
    return null;
  }

  /**
   * 插入关联记录
   *
   * @param modelName 模型名称
   * @param relationObject 关联对象
   * @param parentId 父级ID
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected void insertRelationRecord(String modelName, Object relationObject, Object parentId) {
    log.debug("Inserting relation records for model: {}, parentId: {}", modelName, parentId);

    try {
      Map<String, Object> relationData = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), relationObject, Map.class);
    EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);

      log.debug("Processing {} relation fields for model: {}", relationData.size(), modelName);

      relationData.forEach((fieldName, fieldValue) -> {
        if (fieldValue != null) {
          processRelationFieldInsertion(entity, fieldName, fieldValue, parentId);
        }
      });

      log.debug("Relation record insertion completed for model: {}", modelName);
    } catch (Exception e) {
      log.error("Failed to insert relation records for model: {}, parentId: {}, error: {}", 
                modelName, parentId, e.getMessage(), e);
      throw e;
    }
  }

  /**
   * 处理关联字段的插入
   *
   * @param entity 实体定义
   * @param fieldName 字段名称
   * @param fieldValue 字段值
   * @param parentId 父级ID
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private void processRelationFieldInsertion(EntityDefinition entity, String fieldName, Object fieldValue, Object parentId) {
    if (!(entity.getField(fieldName) instanceof RelationField relationField)) {
      return;
    }

    log.debug("Processing relation field insertion: field={}, relationField={}, parentId={}", 
              fieldName, relationField.getName(), parentId);

    if (relationField.isMultiple()) {
      // 处理一对多关联
      Collection<?> relationCollection = (Collection) fieldValue;
      log.debug("Processing one-to-many relation: {} items", relationCollection.size());
      
      relationCollection.forEach(relationItem -> {
        Map<String, Object> relationRecord = ReflectionUtils.toClassBean(
          sessionContext.getJsonObjectConverter(), relationItem, Map.class);
        relationRecord.put(relationField.getForeignField(), parentId);
        
        log.debug("Inserting relation record: {} -> {}", relationField.getFrom(), relationRecord);
        getDataService().insert(relationField.getFrom(), relationRecord);
      });
    } else {
      // 处理一对一关联
      Map<String, Object> relationRecord = ReflectionUtils.toClassBean(
        sessionContext.getJsonObjectConverter(), fieldValue, Map.class);
      relationRecord.put(relationField.getForeignField(), parentId);
      
      log.debug("Inserting relation record: {} -> {}", relationField.getFrom(), relationRecord);
      getDataService().insert(relationField.getFrom(), relationRecord);
    }
  }
}
