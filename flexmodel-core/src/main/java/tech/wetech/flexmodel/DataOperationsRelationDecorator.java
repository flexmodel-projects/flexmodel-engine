package tech.wetech.flexmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author cjbi
 */
public class DataOperationsRelationDecorator extends AbstractDataOperationsDecorator {

  private final String schemaName;
  private final MappedModels mappedModels;

  public DataOperationsRelationDecorator(DataOperations delegate, String schemaName, MappedModels mappedModels) {
    super(delegate);
    this.schemaName = schemaName;
    this.mappedModels = mappedModels;
  }

  @Override
  public <T> List<T> find(String modelName, Query query, Class<T> resultType) {
    QueryHelper.validate(query);
    List<T> list = super.find(modelName, query, resultType);
    return fillAssignField(delegate, mappedModels.getModel(schemaName, modelName), query, list, resultType);
  }

  record FetchTask(String key, DataOperations dataOperations,
                   AssociationField associationField,
                   Object id) implements Callable<FetchResult> {
    @Override
    public FetchResult call() {
      return new FetchResult(key, dataOperations.find(associationField.targetEntity(), queryInner ->
        queryInner.setFilter(String.format("""
            {
              "==":[{ "var": ["%s"] },%s]
            }
            """, associationField.targetField()
          , id instanceof Number ? id : "\"" + id + "\""
        ))
      ));
    }
  }

  record FetchResult(String key, List<Map<String, Object>> relationData) {
  }

  public <T> List<T> fillAssignField(DataOperations dataOperations, Model model, Query query, List<T> list, Class<T> resultType) {
    if (model instanceof Entity entity) {
      Map<String, List<Map<String, Object>>> fetchResultMap = new HashMap<>();
      Map<String, AssociationField> associationFields = QueryHelper.findAssociationFields(model, query);
      try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
        List<FetchTask> tasks = new ArrayList<>();
        for (Map.Entry<String, AssociationField> fieldEntry : associationFields.entrySet()) {
          for (T data : list) {
            Map dataMap = JsonUtils.getInstance().convertValue(data, Map.class);
            Object id = dataMap.get(entity.idField().name());
            tasks.add(new FetchTask(getRelationKey(model, fieldEntry, id), dataOperations, fieldEntry.getValue(), id));
          }
        }
        List<Future<FetchResult>> resultList = executorService.invokeAll(tasks);
        for (Future<FetchResult> future : resultList) {
          FetchResult fetchResult = future.get();
          fetchResultMap.put(fetchResult.key(), fetchResult.relationData());
        }
        List<T> result = new ArrayList<>();

        for (T data : list) {
          Map dataMap = JsonUtils.getInstance().convertValue(data, Map.class);
          for (Map.Entry<String, AssociationField> fieldEntry : associationFields.entrySet()) {
            Object id = dataMap.get(entity.idField().name());
            dataMap.put(fieldEntry.getKey(), fetchResultMap.get(getRelationKey(model, fieldEntry, id)));
          }
          result.add(JsonUtils.getInstance().convertValue(dataMap, resultType));
        }
        return result;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return list;
  }

  private String getRelationKey(Model model, Map.Entry<String, AssociationField> fieldEntry, Object id) {
    return model.name() + "<" + id + ">" + "@" + fieldEntry.getKey();
  }
}
