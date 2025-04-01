package tech.wetech.flexmodel.parser;

import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.parser.impl.ModelParser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author cjbi
 */
public class SchemaObjectConverter {

  public static SchemaObject toSchemaObject(ModelParser.ASTNode astNode) {
    switch (astNode) {
      case ModelParser.Model sdlModel -> {
        return toSchemaEntity(sdlModel);
      }
      case ModelParser.Enumeration enumeration -> {
        return toSchemaEnum(enumeration);
      }
      default -> {
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static Entity toSchemaEntity(ModelParser.Model sdlModel) {
    Entity entity = new Entity(sdlModel.name);
    for (ModelParser.Field field : sdlModel.fields) {
      entity.addField(toSchemaField(field));
    }
    // 处理模型级别的注解
    for (ModelParser.Annotation mAnno : sdlModel.annotations) {
      if (mAnno.name.equals("comment")) {
        entity.setComment((String) mAnno.parameters.get("value"));
      }
      // 处理索引语法
      if (mAnno.name.equals("index")) {
        Index index = new Index(entity.getName(), mAnno.name);
        if (mAnno.parameters.containsKey("unique")) {
          boolean unique = Boolean.parseBoolean((String) mAnno.parameters.get("unique"));
          index.setUnique(unique);
        }
        List fields = (List) mAnno.parameters.get("fields");
        for (Object field : fields) {
          if (field instanceof String fieldName) {
            index.addField(fieldName);
          } else if (field instanceof Map<?, ?> fieldMap) {
            Set<? extends Map.Entry<?, ?>> entries = fieldMap.entrySet();
            for (Map.Entry<?, ?> entry : entries) {
              String fieldName = (String) entry.getKey();
              Map value = (Map) entry.getValue();
              index.addField(fieldName, Direction.fromString((String) value.get("sort")));
            }
          }
        }
        entity.addIndex(index);
      }
    }
    return entity;
  }

  public static TypedField<?, ?> toSchemaField(ModelParser.Field sdlField) {
    TypedField<?, ?> field = switch (sdlField.type) {
      case ScalarType.ID_TYPE -> {
        field = new IDField(sdlField.name);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          switch (annotation.name) {
            case "generatedValue" ->
              ((IDField) field).setGeneratedValue(IDField.GeneratedValue.valueOf((String) annotation.parameters.get("value")));
            case "comment" -> field.setComment((String) annotation.parameters.get("value"));
            case "unique" -> field.setUnique(true);
          }
        }
        yield field;
      }
      case ScalarType.STRING_TYPE -> {
        field = new StringField(sdlField.name);
        field.setNullable(sdlField.optional);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          if (annotation.name.equals("length")) {
            ((StringField) field).setLength(Integer.parseInt((String) annotation.parameters.get("value")));
          } else if (annotation.name.equals("default")) {
            ((StringField) field).setDefaultValue((String) annotation.parameters.get("value"));
          }
        }

        yield field;
      }
      case ScalarType.FLOAT_TYPE -> {
        field = new FloatField(sdlField.name);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          switch (annotation.name) {
            case "precision" ->
              ((FloatField) field).setPrecision(Integer.parseInt((String) annotation.parameters.get("value")));
            case "scale" ->
              ((FloatField) field).setScale(Integer.parseInt((String) annotation.parameters.get("value")));
            case "default" ->
              ((FloatField) field).setDefaultValue(Double.valueOf((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.INT_TYPE -> {
        field = new IntField(sdlField.name);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          if (annotation.name.equals("default")) {
            ((IntField) field).setDefaultValue(Integer.valueOf((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.LONG_TYPE -> {
        field = new LongField(sdlField.name);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          if (annotation.name.equals("default")) {
            ((LongField) field).setDefaultValue(Long.valueOf((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.BOOLEAN_TYPE -> {
        field = new BooleanField(sdlField.name);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          if (annotation.name.equals("default")) {
            ((BooleanField) field).setDefaultValue(Boolean.valueOf((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.DATETIME_TYPE -> {
        field = new DateTimeField(sdlField.name);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          if (annotation.name.equals("default")) {
            ((DateTimeField) field).setDefaultValue(LocalDateTime.parse((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.DATE_TYPE -> {
        field = new DateField(sdlField.name);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          if (annotation.name.equals("default")) {
            ((DateField) field).setDefaultValue(LocalDate.parse((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.TIME_TYPE -> {
        field = new TimeField(sdlField.name);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          if (annotation.name.equals("default")) {
            ((TimeField) field).setDefaultValue(LocalTime.parse((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.JSON_TYPE -> {
        field = new JSONField(sdlField.name);
        for (ModelParser.Annotation annotation : sdlField.annotations) {
          if (annotation.name.equals("default")) {
            ((JSONField) field).setDefaultValue((String) annotation.parameters.get("value"));
          }
        }
        yield field;
      }
      default -> {
        ModelParser.Annotation relationAnno = sdlField.annotations.stream()
          .filter(f -> f.name.equals("relation")).findFirst()
          .orElse(null);
        boolean isRelationField = relationAnno != null;
        String realName = sdlField.name.replace("[]", "");
        boolean multiple = sdlField.name.endsWith("[]");
        if (isRelationField) {
          RelationField relationField = new RelationField(realName);
          relationField.setMultiple(multiple);
          relationField.setLocalField((String) relationAnno.parameters.get("localField"));
          relationField.setForeignField((String) relationAnno.parameters.get("foreignField"));
          relationField.setCascadeDelete(Boolean.parseBoolean(Objects.toString(relationAnno.parameters.get("cascadeDelete"))));
          field = relationField;
        } else {
          field = new EnumField(realName);
          ((EnumField) field).setFrom(realName);
          ((EnumField) field).setMultiple(multiple);
        }
        yield field;
      }
    };
    // 处理公共属性
    field.setNullable(sdlField.optional);
    for (ModelParser.Annotation annotation : sdlField.annotations) {
      switch (annotation.name) {
        case "comment" -> field.setComment((String) annotation.parameters.get("value"));
        case "unique" -> field.setUnique(true);
        case "additional" -> field.setAdditionalProperties(annotation.parameters);
      }
    }
    return field;
  }

  public static Enum toSchemaEnum(ModelParser.Enumeration sdlEnum) {
    Enum anEnum = new Enum(sdlEnum.name);
    anEnum.setElements(sdlEnum.elements);
    return anEnum;
  }

}
