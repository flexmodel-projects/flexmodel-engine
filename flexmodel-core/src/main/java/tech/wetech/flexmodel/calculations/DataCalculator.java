package tech.wetech.flexmodel.calculations;

import tech.wetech.flexmodel.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class DataCalculator {

  private final String schemaName;
  private final MappedModels mappedModels;
  private final Map<String, ? extends TypeHandler<?>> typeHandlerMap;

  public DataCalculator(String schemaName, MappedModels mappedModels, Map<String, ? extends TypeHandler<?>> typeHandlerMap) {
    this.schemaName = schemaName;
    this.mappedModels = mappedModels;
    this.typeHandlerMap = typeHandlerMap;
  }

  public Map<String, Object> calculateAll(String modelName, Map<String, Object> data) throws ValueCalculateException {
    return calculate(modelName, data, true);
  }

  public Map<String, Object> calculate(String modelName, Map<String, Object> data) throws ValueCalculateException {
    return calculate(modelName, data, false);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private Map<String, Object> calculate(String modelName, Map<String, Object> data, boolean calculateAll) throws ValueCalculateException {
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    List<TypedField<?, ?>> fields = entity.fields();
    Map<String, Object> newData = new HashMap<>();
    for (TypedField<?, ?> field : fields) {
      if (field instanceof AssociationField) {
        continue;
      }
      boolean flag = !calculateAll && !data.containsKey(field.name());
      if (flag) {
        continue;
      }
      if (field.calculators().isEmpty() && data.containsKey(field.name())) {
        newData.put(field.name(), getTypeHandler(field instanceof IDField idField ? idField.generatedValue().type() : field.type())
          .convertParameter(data.get(field.name()))
        );
        continue;
      }
      for (ValueCalculator calculator : field.calculators()) {
        newData.put(field.name(), getTypeHandler(field instanceof IDField idField ? idField.generatedValue().type() : field.type())
          .convertParameter(calculator.calculate(field, data))
        );
      }
    }
    return newData;
  }

  private TypeHandler<?> getTypeHandler(String type) {
    return typeHandlerMap.get(type);
  }


}
