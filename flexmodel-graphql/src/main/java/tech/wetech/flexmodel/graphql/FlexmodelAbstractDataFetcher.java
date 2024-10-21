package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.supports.jackson.JacksonObjectConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.Projections.field;

/**
 * @author cjbi
 */
public abstract class FlexmodelAbstractDataFetcher<T> implements DataFetcher<T> {

  protected final String schemaName;
  protected final String modelName;
  protected final SessionFactory sessionFactory;

  protected static final JsonObjectConverter jsonObjectConverter = new JacksonObjectConverter();

  protected static final String PAGE_NUMBER = "page";
  protected static final String PAGE_SIZE = "size";
  protected static final String ORDER_BY = "order_by";
  protected static final String WHERE = "where";
  protected static final String ID = "id";
  protected static final String AFFECTED_ROWS = "affected_rows";
  protected static final String AGG_COUNT = "_count";
  protected static final String AGG_MAX = "_max";
  protected static final String AGG_MIN = "_min";
  protected static final String AGG_SUM = "_sum";
  protected static final String AGG_AVG = "_avg";
  protected static final String[] AGG_FIELDS = new String[]{AGG_COUNT, AGG_MAX, AGG_MIN, AGG_SUM, AGG_AVG};


  public FlexmodelAbstractDataFetcher(String schemaName, String modelName, SessionFactory sessionFactory) {
    this.schemaName = schemaName;
    this.modelName = modelName;
    this.sessionFactory = sessionFactory;
  }

  protected List<Map<String, Object>> findAssociationDataList(Session session, DataFetchingEnvironment env, String path, String modelName, RelationField relationField, Object id) {
    Entity entity = (Entity) session.getModel(relationField.getModelName());
    Entity targetEntity = (Entity) session.getModel(relationField.getTargetEntity());
    path = path == null ? relationField.getName() : path + "/" + relationField.getName();
    List<SelectedField> selectedFields = env.getSelectionSet().getFields(path + "/*");
    List<RelationField> relationFields = new ArrayList<>();
    List<Map<String, Object>> list = session.find(entity.getName(), query -> query
      .setProjection(projection -> {
        IDField idField = entity.findIdField().orElseThrow();
        projection.addField(idField.getName(), field(entity.getName() + "." + idField.getName()));
        for (SelectedField selectedField : selectedFields) {
          TypedField<?, ?> flexModelField = targetEntity.getField(selectedField.getName());
          if (flexModelField == null) {
            continue;
          }
          if (flexModelField instanceof RelationField secondaryRelationField) {
            relationFields.add(secondaryRelationField);
            continue;
          }
          projection.addField(selectedField.getName(), field(targetEntity.getName() + "." + flexModelField.getName()));
        }
        return projection;
      })
      .setJoins(joins -> joins.addLeftJoin(join -> join.setFrom(targetEntity.getName())))
      .setFilter(f -> f.equalTo(entity.getName() + "." + entity.findIdField().map(IDField::getName).orElseThrow(), id))
    );
    List<Map<String, Object>> result = new ArrayList<>();
    for (Map<String, Object> map : list) {
      Map<String, Object> resultData = new HashMap<>(map);
      result.add(resultData);
      for (RelationField sencondaryRelationField : relationFields) {
        Object secondaryId = map.get(entity.findIdField().map(IDField::getName).orElseThrow());
        resultData.put(sencondaryRelationField.getName(),
          sencondaryRelationField.getCardinality() == RelationField.Cardinality.ONE_TO_ONE ?
            findAssociationDataList(session, env, path, sencondaryRelationField.getTargetEntity(), sencondaryRelationField, secondaryId).stream()
              .findFirst()
              .orElse(null)
            : findAssociationDataList(session, env, path, sencondaryRelationField.getTargetEntity(), sencondaryRelationField, secondaryId));
      }
    }
    return result;
  }

  protected String getFilterString(Map<String, Object> where) {
    String filter;
    if (where != null && !where.isEmpty()) {
      filter = jsonObjectConverter.toJsonString(FlexmodelInputWhereTransformer.transform(where));
    } else {
      filter = null;
    }
    return filter;
  }

}
