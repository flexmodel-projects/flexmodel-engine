package tech.wetech.flexmodel.graphql;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import tech.wetech.flexmodel.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.wetech.flexmodel.Projections.field;

/**
 * @author cjbi
 */
public class FlexmodelDataFetcher implements DataFetcher<List<Map<String, Object>>> {

  private final String schemaName;
  private final SessionFactory sessionFactory;

  public FlexmodelDataFetcher(String schemaName, SessionFactory sessionFactory) {
    this.schemaName = schemaName;
    this.sessionFactory = sessionFactory;
  }


  @Override
  public List<Map<String, Object>> get(DataFetchingEnvironment env) throws Exception {
    String queryFieldName = env.getMergedField().getSingleField().getName();
    return findRootData(env, queryFieldName.replaceFirst(schemaName + "_", ""));
  }

  public List<Map<String, Object>> findRootData(DataFetchingEnvironment env, String modelName) {
    List<SelectedField> selectedFields = env.getSelectionSet().getImmediateFields();
    try (Session session = sessionFactory.createSession(schemaName)) {
      Entity entity = (Entity) session.getModel(modelName);
      List<RelationField> relationFields = new ArrayList<>();
      List<Map<String, Object>> list = session.find(entity.getName(), query -> query
        .setProjection(projection -> {
          IDField idField = entity.findIdField().orElseThrow();
          projection.addField(idField.getName(), field(entity.getName() + "." + idField.getName()));
          for (SelectedField selectedField : selectedFields) {
            TypedField<?, ?> flexModelField = (TypedField<?, ?>) entity.getField(selectedField.getName());
            if (flexModelField == null) {
              continue;
            }
            if (flexModelField instanceof RelationField secondaryRelationField) {
              relationFields.add(secondaryRelationField);
              continue;
            }
            projection.addField(selectedField.getName(), field(flexModelField.getModelName() + "." + flexModelField.getName()));
          }
          return projection;
        })
      );
      List<Map<String, Object>> result = new ArrayList<>();
      for (Map<String, Object> map : list) {
        Map<String, Object> resultData = new HashMap<>(map);
        result.add(resultData);
        for (RelationField sencondaryRelationField : relationFields) {
          Object secondaryId = map.get(entity.findIdField().map(IDField::getName).orElseThrow());
          resultData.put(sencondaryRelationField.getName(),
            sencondaryRelationField.getCardinality() == RelationField.Cardinality.ONE_TO_ONE ?
              findAssociationDataList(session, env, null, sencondaryRelationField.getTargetEntity(), sencondaryRelationField, secondaryId).stream()
                .findFirst()
                .orElse(null)
              : findAssociationDataList(session, env, null, sencondaryRelationField.getTargetEntity(), sencondaryRelationField, secondaryId));
        }
      }
      return result;
    }

  }

  public List<Map<String, Object>> findAssociationDataList(Session session, DataFetchingEnvironment env, String path, String modelName, RelationField relationField, Object id) {
    Entity entity = (Entity) session.getModel(relationField.getModelName());
    Entity targetEntity = (Entity) session.getModel(relationField.getTargetEntity());
    path = path == null ? relationField.getName() : path + "/" + relationField.getName();
    List<SelectedField> selectedFields = env.getSelectionSet().getFields(path + "/*");
    String filter = String.format("""
        {
          "==": [
            {"var": ["%s"]},
            %s
          ]
        }
        """,
      entity.getName() + "." + entity.findIdField().map(IDField::getName).orElseThrow(),
      id instanceof Number ? id : "\"" + id + "\""
    );
    List<RelationField> relationFields = new ArrayList<>();
    List<Map<String, Object>> list = session.find(entity.getName(), query -> query
      .setProjection(projection -> {
        IDField idField = entity.findIdField().orElseThrow();
        projection.addField(idField.getName(), field(entity.getName() + "." + idField.getName()));
        for (SelectedField selectedField : selectedFields) {
          TypedField<?, ?> flexModelField = (TypedField<?, ?>) targetEntity.getField(selectedField.getName());
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
      .setFilter(filter)
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

}
