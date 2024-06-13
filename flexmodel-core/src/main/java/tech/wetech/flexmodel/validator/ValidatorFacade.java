package tech.wetech.flexmodel.validator;

import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.MappedModels;
import tech.wetech.flexmodel.RelationField;
import tech.wetech.flexmodel.TypedField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class ValidatorFacade {

  private final String schemaName;
  private final MappedModels mappedModels;

  public ValidatorFacade(String schemaName, MappedModels mappedModels) {
    this.schemaName = schemaName;
    this.mappedModels = mappedModels;
  }

  public void validateAll(String modelName, Map<String, Object> data) {
    validate(modelName, data, true);
  }

  public void validate(String modelName, Map<String, Object> data) {
    validate(modelName, data, false);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void validate(String modelName, Map<String, Object> data, boolean validAll) {
    Entity entity = (Entity) mappedModels.getModel(schemaName, modelName);
    List<TypedField<?, ?>> fields = entity.getFields();
    List<ConstraintValidException> errors = new ArrayList<>();
    for (TypedField<?, ?> field : fields) {
      boolean flag = !validAll && !data.containsKey(field.getName());
      if (flag) {
        continue;
      }
      if (field instanceof RelationField) {
        continue;
      }
      for (ConstraintValidator validator : field.getValidators()) {
        try {
          validator.validate(field, data.get(field.getName()));
        } catch (ConstraintValidException e) {
          errors.add(e);
        }
      }
    }
    if (!errors.isEmpty()) {
      throw new DataValidException(errors);
    }
  }

}
