package tech.wetech.flexmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.wetech.flexmodel.generator.ULID;
import tech.wetech.flexmodel.reflect.LazyObjProxy;
import tech.wetech.flexmodel.reflect.ReflectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Supplier;

/**
 * 统一的Session实现，完全合并了所有装饰器和中间层功能
 * 消除了所有中间层和装饰器，直接实现Session接口
 *
 * @author cjbi
 */
public abstract class AbstractSession implements Session {

    private static final Logger log = LoggerFactory.getLogger(AbstractSession.class);

    private final String schemaName;
    private final SessionFactory factory;
    private final AbstractSessionContext sessionContext;
    private final DataOperations dataOperations;
    private final SchemaOperations schemaOperations;

    public AbstractSession(AbstractSessionContext sessionContext,
                          DataOperations dataOperations,
                          SchemaOperations schemaOperations) {
        this.sessionContext = sessionContext;
        this.schemaName = sessionContext.getSchemaName();
        this.factory = sessionContext.getFactory();
        this.dataOperations = dataOperations;
        this.schemaOperations = schemaOperations;
    }

    // ==================== Schema操作 (内联了SchemaOperationsPersistenceDecorator) ====================

    @Override
    public List<SchemaObject> syncModels() {
        return schemaOperations.syncModels();
    }

    @Override
    public List<SchemaObject> syncModels(Set<String> modelNames) {
        return schemaOperations.syncModels(modelNames);
    }

    @Override
    public List<SchemaObject> getAllModels() {
        return schemaOperations.getAllModels();
    }

    @Override
    public SchemaObject getModel(String modelName) {
        return schemaOperations.getModel(modelName);
    }

    @Override
    public void dropModel(String modelName) {
        inspect(() -> schemaOperations.dropModel(modelName));
        sessionContext.getMappedModels().remove(schemaName, modelName);
    }

    @Override
    public EntityDefinition createEntity(EntityDefinition collection) {
        inspect(() -> schemaOperations.createEntity(collection));
        sessionContext.getMappedModels().persist(schemaName, collection);
        return collection;
    }

    @Override
    public NativeQueryDefinition createNativeQueryModel(NativeQueryDefinition model) {
        schemaOperations.createNativeQueryModel(model);
        sessionContext.getMappedModels().persist(schemaName, model);
        return model;
    }

    @Override
    public EnumDefinition createEnum(EnumDefinition anEnum) {
        schemaOperations.createEnum(anEnum);
        sessionContext.getMappedModels().persist(schemaName, anEnum);
        return anEnum;
    }

    @Override
    public TypedField<?, ?> createField(TypedField<?, ?> field) {
        inspect(() -> schemaOperations.createField(field));
        EntityDefinition entity = (EntityDefinition) sessionContext.getModel(field.getModelName());
        entity.addField(field);
        sessionContext.getMappedModels().persist(schemaName, entity);
        return field;
    }

    @Override
    public TypedField<?, ?> modifyField(TypedField<?, ?> field) {
        inspect(() -> schemaOperations.modifyField(field));
        EntityDefinition entity = (EntityDefinition) sessionContext.getModel(field.getModelName());
        entity.removeField(field.getName());
        entity.addField(field);
        sessionContext.getMappedModels().persist(schemaName, entity);
        return field;
    }

    @Override
    public void dropField(String modelName, String fieldName) {
        inspect(() -> schemaOperations.dropField(modelName, fieldName));
        EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
        entity.removeField(fieldName);
        // 移除相关索引
        for (Index index : entity.getIndexes()) {
            if (index.containsField(fieldName)) {
                entity.removeIndex(index.getName());
            }
        }
        sessionContext.getMappedModels().persist(schemaName, entity);
    }

    @Override
    public Index createIndex(Index index) {
        inspect(() -> schemaOperations.createIndex(index));
        EntityDefinition entity = (EntityDefinition) sessionContext.getModel(index.getModelName());
        entity.addIndex(index);
        sessionContext.getMappedModels().persist(schemaName, entity);
        return index;
    }

    @Override
    public void dropIndex(String modelName, String indexName) {
        inspect(() -> schemaOperations.dropIndex(modelName, indexName));
        EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
        entity.removeIndex(indexName);
        sessionContext.getMappedModels().persist(schemaName, entity);
    }

    @Override
    public void createSequence(String sequenceName, int initialValue, int incrementSize) {
        schemaOperations.createSequence(sequenceName, initialValue, incrementSize);
    }

    @Override
    public void dropSequence(String sequenceName) {
        schemaOperations.dropSequence(sequenceName);
    }

    @Override
    public long getSequenceNextVal(String sequenceName) {
        return schemaOperations.getSequenceNextVal(sequenceName);
    }

    // ==================== 数据操作 (内联了DataOperationsGenerationDecorator) ====================

    @Override
    public int insert(String modelName, Object record) {
        Map<String, Object> data = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), record, Map.class);
        Map<String, Object> processedData = generateValue(modelName, data, false);

        int rows = dataOperations.insert(modelName, processedData);

        // 获取生成的ID（如果有的话）
        EntityDefinition entity = (EntityDefinition) sessionContext.getModel(modelName);
        Optional<TypedField<?, ?>> idFieldOptional = entity.findIdField();
        Object id = null;
        if (idFieldOptional.isPresent()) {
            id = processedData.get(idFieldOptional.get().getName());
            // 将生成的ID放回到原始的data map中
            data.put(idFieldOptional.get().getName(), id);
        }

        // 处理关联关系
        insertRelationRecord(modelName, data, id);
        return rows;
    }

    @Override
    public <T> T findById(String modelName, Object id, Class<T> resultType, boolean nestedQuery) {
        T result = dataOperations.findById(modelName, id, resultType, nestedQuery);
        return LazyObjProxy.createProxy(result, modelName, sessionContext);
    }

    @Override
    public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
        List<T> results = dataOperations.find(modelName, query, resultType);
        return LazyObjProxy.createProxyList(results, modelName, sessionContext);
    }

    @Override
    public <T> List<T> findByNativeQuery(String statement, Object params, Class<T> resultType) {
        return dataOperations.findByNativeQuery(statement, params, resultType);
    }

    @Override
    public <T> List<T> findByNativeQueryModel(String modelName, Object params, Class<T> resultType) {
        return dataOperations.findByNativeQueryModel(modelName, params, resultType);
    }

    @Override
    public long count(String modelName, Query query) {
        return dataOperations.count(modelName, query);
    }

    @Override
    public int updateById(String modelName, Object record, Object id) {
        Map<String, Object> data = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), record, Map.class);
        Map<String, Object> processedData = generateValue(modelName, data, true);
        return dataOperations.updateById(modelName, processedData, id);
    }

    @Override
    public int update(String modelName, Object record, String filter) {
        Map<String, Object> data = ReflectionUtils.toClassBean(sessionContext.getJsonObjectConverter(), record, Map.class);
        Map<String, Object> processedData = generateValue(modelName, data, true);
        return dataOperations.update(modelName, processedData, filter);
    }

    @Override
    public int deleteById(String modelName, Object id) {
        return dataOperations.deleteById(modelName, id);
    }

    @Override
    public int delete(String modelName, String filter) {
        return dataOperations.delete(modelName, filter);
    }

    @Override
    public int deleteAll(String modelName) {
        return dataOperations.deleteAll(modelName);
    }

    @Override
    public SessionFactory getFactory() {
        return factory;
    }

    @Override
    public String getName() {
        return schemaName;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 生成字段值（内联了DataOperationsGenerationDecorator的逻辑）
     */
    private Map<String, Object> generateValue(String modelName, Map<String, Object> data, boolean isUpdate) {
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
            if (field.getDefaultValue() != null && newData.containsKey(field.getName())) {
                newData.put(field.getName(), generateFieldValue(field, value, isUpdate));
            }
        }

        return newData;
    }

    private Object convertParameter(TypedField<?, ?> field, Object value) {
        return sessionContext.getTypeHandlerMap().get(field.getType())
            .convertParameter(field, value);
    }

    private Object generateFieldValue(TypedField<?, ?> field, Object value, boolean isUpdate) {
        if (!isUpdate && value == null) {
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

    /**
     * 处理关联关系记录（内联了DataOperationsGenerationDecorator的逻辑）
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void insertRelationRecord(String modelName, Object objR, Object id) {
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
                            insert(relationField.getFrom(), relationRecord);
                        });
                    } else {
                        Map<String, Object> relationRecord = ReflectionUtils.toClassBean(
                            sessionContext.getJsonObjectConverter(), value, Map.class);
                        relationRecord.put(relationField.getForeignField(), id);
                        insert(relationField.getFrom(), relationRecord);
                    }
                }
            }
        });
    }

    /**
     * 故障安全模式检查（内联了SchemaOperationsPersistenceDecorator的逻辑）
     */
    private <T> T inspect(Supplier<T> supplier, T orElse) {
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

    private void inspect(Runnable runnable) {
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
