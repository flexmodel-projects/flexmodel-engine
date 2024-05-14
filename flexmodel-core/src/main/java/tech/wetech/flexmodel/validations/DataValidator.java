package tech.wetech.flexmodel.validations;

import tech.wetech.flexmodel.AssociationField;
import tech.wetech.flexmodel.Entity;
import tech.wetech.flexmodel.MappedModels;
import tech.wetech.flexmodel.TypedField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author cjbi
 */
public class DataValidator {

  private final String schemaName;
  private final MappedModels mappedModels;

  public DataValidator(String schemaName, MappedModels mappedModels) {
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
    Entity entity = mappedModels.getEntity(schemaName, modelName);
    List<TypedField<?, ?>> fields = entity.getFields();
    List<ConstraintValidException> errors = new ArrayList<>();
    for (TypedField<?, ?> field : fields) {
      boolean flag = !validAll && !data.containsKey(field.getName());
      if (flag) {
        continue;
      }
      if (field instanceof AssociationField) {
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
