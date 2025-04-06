package tech.wetech.flexmodel.parser;

import tech.wetech.flexmodel.Enum;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.parser.impl.ModelParser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author cjbi
 */
public class ASTNodeConverter {

  public static SchemaObject toSchemaObject(ModelParser.ASTNode astNode) {
    switch (astNode) {
      case ModelParser.Model idlModel -> {
        return toSchemaEntity(idlModel);
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
  public static Entity toSchemaEntity(ModelParser.Model idlModel) {
    Entity entity = new Entity(idlModel.name);
    for (ModelParser.Field field : idlModel.fields) {
      entity.addField(toSchemaField(field));
    }
    // 处理模型级别的注解
    for (ModelParser.Annotation mAnno : idlModel.annotations) {
      if (mAnno.name.equals("comment")) {
        entity.setComment((String) mAnno.parameters.get("value"));
      }
      // 处理索引语法
      if (mAnno.name.equals("index")) {
        Index index = new Index(entity.getName());
        index.setName((String) mAnno.parameters.get("name"));
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

  public static TypedField<?, ?> toSchemaField(ModelParser.Field idlField) {
    TypedField<?, ?> field = switch (idlField.type) {
      case ScalarType.ID_TYPE -> {
        field = new IDField(idlField.name);
        for (ModelParser.Annotation annotation : idlField.annotations) {
          switch (annotation.name) {
            case "default" -> {
              Object value = annotation.parameters.get("value");
              if (value instanceof ModelParser.FunctionCall func) {
                field.setDefaultValue(new GeneratedValue(func.name));
              }
              yield field;
            }
            case "comment" -> field.setComment((String) annotation.parameters.get("value"));
            case "unique" -> field.setUnique(true);
          }
        }
        yield field;
      }
      case ScalarType.STRING_TYPE -> {
        field = new StringField(idlField.name);
        field.setNullable(idlField.optional);
        for (ModelParser.Annotation annotation : idlField.annotations) {
          if (annotation.name.equals("length")) {
            ((StringField) field).setLength(Integer.parseInt((String) annotation.parameters.get("value")));
          } else if (annotation.name.equals("default")) {
            ((StringField) field).setDefaultValue((String) annotation.parameters.get("value"));
          }
        }

        yield field;
      }
      case ScalarType.FLOAT_TYPE -> {
        field = new FloatField(idlField.name);
        for (ModelParser.Annotation annotation : idlField.annotations) {
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
        field = new IntField(idlField.name);
        for (ModelParser.Annotation annotation : idlField.annotations) {
          if (annotation.name.equals("default")) {
            ((IntField) field).setDefaultValue(Integer.valueOf((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.LONG_TYPE -> {
        field = new LongField(idlField.name);
        for (ModelParser.Annotation annotation : idlField.annotations) {
          if (annotation.name.equals("default")) {
            ((LongField) field).setDefaultValue(Long.valueOf((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.BOOLEAN_TYPE -> {
        field = new BooleanField(idlField.name);
        for (ModelParser.Annotation annotation : idlField.annotations) {
          if (annotation.name.equals("default")) {
            ((BooleanField) field).setDefaultValue(Boolean.valueOf((String) annotation.parameters.get("value")));
          }
        }
        yield field;
      }
      case ScalarType.DATETIME_TYPE -> {
        field = new DateTimeField(idlField.name);
        for (ModelParser.Annotation annotation : idlField.annotations) {
          if (annotation.name.equals("default")) {
            Object value = annotation.parameters.get("value");
            if (value instanceof ModelParser.FunctionCall func) {
              field.setDefaultValue(new GeneratedValue(func.name));
            } else {
              ((DateTimeField) field).setDefaultValue(LocalDateTime.parse((String) annotation.parameters.get("value")));
            }
          }
        }
        yield field;
      }
      case ScalarType.DATE_TYPE -> {
        field = new DateField(idlField.name);
        for (ModelParser.Annotation annotation : idlField.annotations) {
          if (annotation.name.equals("default")) {
            Object value = annotation.parameters.get("value");
            if (value instanceof ModelParser.FunctionCall func) {
              field.setDefaultValue(new GeneratedValue(func.name));
            } else {
              ((DateField) field).setDefaultValue(LocalDate.parse((String) annotation.parameters.get("value")));
            }
          }
        }
        yield field;
      }
      case ScalarType.TIME_TYPE -> {
        field = new TimeField(idlField.name);
        for (ModelParser.Annotation annotation : idlField.annotations) {
          if (annotation.name.equals("default")) {
            Object value = annotation.parameters.get("value");
            if (value instanceof ModelParser.FunctionCall func) {
              field.setDefaultValue(new GeneratedValue(func.name));
            } else {
              ((TimeField) field).setDefaultValue(LocalTime.parse((String) annotation.parameters.get("value")));
            }
          }
        }
        yield field;
      }
      case ScalarType.JSON_TYPE -> {
        field = new JSONField(idlField.name);
        for (ModelParser.Annotation annotation : idlField.annotations) {
          if (annotation.name.equals("default")) {
            ((JSONField) field).setDefaultValue((String) annotation.parameters.get("value"));
          }
        }
        yield field;
      }
      default -> {
        ModelParser.Annotation relationAnno = idlField.annotations.stream()
          .filter(f -> f.name.equals("relation")).findFirst()
          .orElse(null);
        boolean isRelationField = relationAnno != null;
        String from = idlField.type.replace("[]", "");
        boolean multiple = idlField.type.endsWith("[]");
        if (isRelationField) {
          RelationField relationField = new RelationField(idlField.name);
          relationField.setMultiple(multiple);
          relationField.setFrom(from);
          relationField.setLocalField((String) relationAnno.parameters.get("localField"));
          relationField.setForeignField((String) relationAnno.parameters.get("foreignField"));
          relationField.setCascadeDelete(Boolean.parseBoolean(Objects.toString(relationAnno.parameters.get("cascadeDelete"))));
          field = relationField;
        } else {
          field = new EnumField(idlField.name);
          ((EnumField) field).setFrom(from);
          ((EnumField) field).setMultiple(multiple);
        }
        yield field;
      }
    };
    // 处理公共属性
    field.setNullable(idlField.optional);
    for (ModelParser.Annotation annotation : idlField.annotations) {
      switch (annotation.name) {
        case "comment" -> field.setComment((String) annotation.parameters.get("value"));
        case "unique" -> field.setUnique(true);
        case "additional" -> field.setAdditionalProperties(annotation.parameters);
      }
    }
    return field;
  }

  public static Enum toSchemaEnum(ModelParser.Enumeration idlEnum) {
    Enum anEnum = new Enum(idlEnum.name);
    anEnum.setElements(idlEnum.elements);
    return anEnum;
  }

  public static ModelParser.ASTNode fromSchemaObject(SchemaObject schemaObject) {
    if (schemaObject instanceof Entity) {
      return fromSchemaEntity((Entity) schemaObject);
    } else if (schemaObject instanceof Enum) {
      return fromSchemaEnum((Enum) schemaObject);
    }
    return null;
  }

  public static ModelParser.Model fromSchemaEntity(Entity entity) {
    ModelParser.Model model = new ModelParser.Model(entity.getName());

    // 添加字段
    for (TypedField<?, ?> field : entity.getFields()) {
      model.fields.add(fromSchemaField(field));
    }

    // 处理实体级别注解
    if (entity.getComment() != null) {
      ModelParser.Annotation commentAnno = new ModelParser.Annotation("comment");
      commentAnno.parameters.put("value", entity.getComment());
      model.annotations.add(commentAnno);
    }

    // 处理索引
    for (Index index : entity.getIndexes()) {
      ModelParser.Annotation indexAnno = new ModelParser.Annotation("index");
      indexAnno.parameters.put("name", index.getName());
      indexAnno.parameters.put("unique", String.valueOf(index.isUnique()));

      List<Object> fields = new ArrayList<>();
      for (Index.Field indexField : index.getFields()) {
        if (indexField.direction() == Direction.ASC) { // 默认方向可省略
          fields.add(indexField.fieldName());
        } else {
          Map<String, Object> fieldMap = new HashMap<>();
          Map<String, String> value = new HashMap<>();
          value.put("sort", indexField.direction().toString().toLowerCase());
          fieldMap.put(indexField.fieldName(), value);
          fields.add(fieldMap);
        }
      }
      indexAnno.parameters.put("fields", fields);
      model.annotations.add(indexAnno);
    }

    return model;
  }

  public static ModelParser.Field fromSchemaField(TypedField<?, ?> field) {
    ModelParser.Field idlField = new ModelParser.Field(field.getName(), field.isNullable(), getCorrespondingType(field));

    // 处理字段注解
    if (field.getComment() != null) {
      ModelParser.Annotation commentAnno = new ModelParser.Annotation("comment");
      commentAnno.parameters.put("value", field.getComment());
      idlField.annotations.add(commentAnno);
    }

    if (field.isUnique()) {
      idlField.annotations.add(new ModelParser.Annotation("unique"));
    }

    // 类型特定处理
    switch (field) {
      case IDField idField -> {
        if (idField.getDefaultValue() != null) {
          if (idField.getDefaultValue() instanceof GeneratedValue generatedValue) {
            ModelParser.Annotation anno = new ModelParser.Annotation("default");
            anno.parameters.put("value", new ModelParser.FunctionCall(generatedValue.getName()));
            idlField.annotations.add(anno);
          }
        }
      }
      case StringField stringField -> {
        if (stringField.getLength() > 0) {
          ModelParser.Annotation anno = new ModelParser.Annotation("length");
          anno.parameters.put("value", String.valueOf(stringField.getLength()));
          idlField.annotations.add(anno);
        }
        addDefaultAnnotation(idlField, stringField.getDefaultValue());
      }
      case FloatField floatField -> {
        addNumericAnnotations(idlField, floatField.getPrecision(), floatField.getScale());
        addDefaultAnnotation(idlField, floatField.getDefaultValue());
      }
      case IntField intField -> addDefaultAnnotation(idlField, intField.getDefaultValue());
      case LongField longField -> addDefaultAnnotation(idlField, longField.getDefaultValue());
      case BooleanField booleanField -> addDefaultAnnotation(idlField, booleanField.getDefaultValue());
      case DateTimeField dateTimeField -> addDefaultAnnotation(idlField, dateTimeField.getDefaultValue());
      case DateField dateField -> addDefaultAnnotation(idlField, dateField.getDefaultValue());
      case TimeField timeField -> addDefaultAnnotation(idlField, timeField.getDefaultValue());
      case JSONField jsonField -> addDefaultAnnotation(idlField, jsonField.getDefaultValue());
      case RelationField relationField -> {
        ModelParser.Annotation relationAnno = new ModelParser.Annotation("relation");
        relationAnno.parameters.put("localField", relationField.getLocalField());
        relationAnno.parameters.put("foreignField", relationField.getForeignField());
        relationAnno.parameters.put("cascadeDelete",
          String.valueOf(relationField.isCascadeDelete()));
        idlField.annotations.add(relationAnno);
      }
      case EnumField enumField -> {
        // todo
      }

      default -> {
      }
    }

    // 处理额外属性
    if (!field.getAdditionalProperties().isEmpty()) {
      ModelParser.Annotation additionalAnno = new ModelParser.Annotation("additional");
      additionalAnno.parameters.putAll(field.getAdditionalProperties());
      idlField.annotations.add(additionalAnno);
    }

    return idlField;
  }

  public static ModelParser.Enumeration fromSchemaEnum(Enum schemaEnum) {
    ModelParser.Enumeration enumeration = new ModelParser.Enumeration(schemaEnum.getName());
    enumeration.elements = new ArrayList<>(schemaEnum.getElements());
    return enumeration;
  }

  // 辅助方法
  private static String getCorrespondingType(TypedField<?, ?> field) {
    if (field instanceof RelationField relationField) {
      return relationField.isMultiple() ? relationField.getFrom() + "[]" : relationField.getFrom();
    } else if (field instanceof EnumField enumField) {
      return enumField.isMultiple() ? enumField.getFrom() + "[]" : enumField.getFrom();
    } else {
      return field.getType();
    }
  }

  private static void addDefaultAnnotation(ModelParser.Field idlField, Object defaultValue) {
    if (defaultValue != null) {
      ModelParser.Annotation anno = new ModelParser.Annotation("default");
      if (defaultValue instanceof GeneratedValue generatedValue) {
        anno.parameters.put("value", new ModelParser.FunctionCall(generatedValue.getName()));
      } else {
        anno.parameters.put("value", defaultValue.toString());
      }
      idlField.annotations.add(anno);
    }
  }

  private static void addNumericAnnotations(ModelParser.Field idlField,
                                            Integer precision, Integer scale) {
    if (precision != null) {
      ModelParser.Annotation anno = new ModelParser.Annotation("precision");
      anno.parameters.put("value", precision.toString());
      idlField.annotations.add(anno);
    }
    if (scale != null) {
      ModelParser.Annotation anno = new ModelParser.Annotation("scale");
      anno.parameters.put("value", scale.toString());
      idlField.annotations.add(anno);
    }
  }

}
