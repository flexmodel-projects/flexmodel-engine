package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.graph.JoinGraphNode;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import java.util.*;

import static tech.wetech.flexmodel.AssociationField.Cardinality.MANY_TO_MANY;
import static tech.wetech.flexmodel.Query.Join.JoinType.INNER_JOIN;
import static tech.wetech.flexmodel.Query.Join.JoinType.LEFT_JOIN;

/**
 * @author cjbi
 */
class SqlHelper {

  public static String toQuerySql(SqlContext sqlContext, String modelName, Query query) {
    Map.Entry<String, Map<String, Object>> entry = toQuerySql(sqlContext, modelName, query, false);
    return entry.getKey();
  }

  public static Map.Entry<String, Map<String, Object>> toQuerySqlWithPrepared(SqlContext sqlContext, String modelName, Query query) {
    return toQuerySql(sqlContext, modelName, query, true);
  }

  private static Map.Entry<String, Map<String, Object>> toQuerySql(SqlContext sqlContext, String modelName, Query query, boolean prepared) {
    String sqlString;
    Map<String, Object> params = new HashMap<>();
    SqlDialect sqlDialect = sqlContext.getSqlDialect();
    Model model = sqlContext.getMappedModels().getModel(sqlContext.getSchemaName(), modelName);
    String physicalFromTableName = toPhysicalTableNameQuoteString(sqlContext, modelName);
    StringBuilder sql = new StringBuilder("\nselect ");
    Query.Projection projection = query.getProjection();
    Map<String, String> aliasColumnMap = new HashMap<>();
    Map<String, AssociationField> associationFields = QueryHelper.findAssociationFields(model, query);
    StringJoiner columns = new StringJoiner(", ");
    if (projection != null) {
      for (Map.Entry<String, Query.QueryCall> entry : projection.getFields().entrySet()) {
        Query.QueryCall value = entry.getValue();
        String key = entry.getKey();
        if (associationFields.containsKey(key)) {
          // 不查关联字段
          continue;
        }
        String sqlCall = toSqlCall(sqlContext, value);
        aliasColumnMap.put(key, sqlCall);
        columns.add("\n " + sqlCall + " " + sqlDialect.quoteIdentifier(key));
      }
    } else {
      for (Field field : model.getFields()) {
        if (associationFields.containsKey(field.getName())) {
          // 不查关联字段
          continue;
        }
        columns.add("\n " + toFullColumnQuoteString(sqlContext, modelName, field.getName()) + " " + sqlDialect.quoteIdentifier(field.getName()));
      }
    }
    sql.append(columns);
    sql.append("\nfrom ").append(physicalFromTableName);
    Query.Joins joins = query.getJoins();
    if (joins != null) {
      StringBuilder joinCause = new StringBuilder();
      for (Query.Join joiner : joins.getJoins()) {
        String joinTableName = toPhysicalTableNameQuoteString(sqlContext, joiner.getFrom());
        if (joiner.getJoinType() == LEFT_JOIN) {
          joinCause.append("\nleft join ");
        }
        if (joiner.getJoinType() == INNER_JOIN) {
          joinCause.append("\ninner join ");
        }
        String localField = joiner.getLocalField();
        String foreignField = joiner.getForeignField();
        AssociationField associationField;
        if (model instanceof Entity entity &&
            (associationField = entity.findAssociationFieldByEntityName(joiner.getFrom()).orElse(null)) != null) {
          localField = entity.getIdField().getName();
          foreignField = associationField.getTargetField();
          if (associationField.getCardinality() == MANY_TO_MANY) {
            Entity targetEntity = sqlContext.getMappedModels().getEntity(sqlContext.getSchemaName(), associationField.getTargetEntity());
            JoinGraphNode joinGraphNode = new JoinGraphNode(entity, targetEntity, associationField);
            joinCause.append(toPhysicalTableNameQuoteString(sqlContext, joinGraphNode.getJoinName()))
              .append(" \n on \n")
              .append(toFullColumnQuoteString(sqlContext, modelName, localField))
              .append("=")
              .append(toFullColumnQuoteString(sqlContext, joinGraphNode.getJoinName(), joinGraphNode.getJoinFieldName()));
            if (joiner.getJoinType() == LEFT_JOIN) {
              joinCause.append("\nleft join ");
            }
            if (joiner.getJoinType() == INNER_JOIN) {
              joinCause.append("\ninner join ");
            }
            joinCause.append(joinTableName)
              .append(" \n on \n")
              .append(toFullColumnQuoteString(sqlContext, joiner.getFrom(), foreignField))
              .append("=")
              .append(toFullColumnQuoteString(sqlContext, joinGraphNode.getJoinName(), joinGraphNode.getInverseJoinFieldName()));
          } else {
            joinCause.append(joinTableName)
              .append(" \n on \n")
              .append(toFullColumnQuoteString(sqlContext, modelName, localField))
              .append("=")
              .append(toFullColumnQuoteString(sqlContext, joiner.getFrom(), foreignField));
          }
        } else {
          joinCause.append(joinTableName)
            .append(" \n on \n")
            .append(toFullColumnQuoteString(sqlContext, modelName, localField))
            .append("=")
            .append(toFullColumnQuoteString(sqlContext, joiner.getFrom(), foreignField));
        }
        StringBuilder joinCondition = new StringBuilder();
        if (joiner.getFilter() != null) {
          if (prepared) {
            SqlClauseResult leftSqlWhere = toSqlWhereClauseWithPrepared(sqlContext, joiner.getFilter());
            joinCondition.append(" and ")
              .append(leftSqlWhere.sqlClause());
            params.putAll(leftSqlWhere.args());
          } else {
            joinCondition.append(" and ")
              .append(toSqlWhereClause(sqlContext, joiner.getFilter()));
          }
          joinCause.append(joinCondition);
        }
      }
      sql.append(joinCause);
    }
    if (query.getFilter() != null) {
      if (prepared) {
        SqlClauseResult sqlClauseResult = toSqlWhereClauseWithPrepared(sqlContext, query.getFilter());
        sql.append("\nwhere (").append(sqlClauseResult.sqlClause()).append(")");
        params.putAll(sqlClauseResult.args());
      } else {
        sql.append("\nwhere (").append(toSqlWhereClause(sqlContext, query.getFilter())).append(")");
      }
    }
    if (query.getGroupBy() != null) {
      sql.append("\ngroup by ");
      StringJoiner groupByColumns = new StringJoiner(", ");
      for (Query.QueryField field : query.getGroupBy().getFields()) {
        groupByColumns.add(sqlDialect.supportsGroupByColumnAlias()
          ? toFullColumnQuoteString(sqlContext, field.getModelName(), field.getFieldName())
          : aliasColumnMap.getOrDefault(field.getFieldName(), toFullColumnQuoteString(sqlContext, field.getModelName(), field.getFieldName())));
      }
      sql.append(groupByColumns);
    }
    Query.Sort sort = query.getSort();
    if (sort != null) {
      sql.append("\norder by ");
      StringJoiner sortColumns = new StringJoiner(", ");
      for (Query.Sort.Order order : sort.getOrders()) {
        sortColumns.add(toFullColumnQuoteString(sqlContext, order.getField().getModelName(), order.getField().getFieldName()) + " " + order.getDirection().name().toLowerCase());
      }
      sql.append(sortColumns);
    }
    if (query.getLimit() != null) {
      sqlString = sqlDialect.getLimitString(sql.toString(),
        Objects.toString(query.getOffset(), null),
        query.getLimit().toString());
    } else {
      sqlString = sql.toString();
    }
    return new AbstractMap.SimpleEntry<>(sqlString, params);
  }

  private static String toSqlCall(SqlContext sqlContext, Query.QueryCall queryCall) {
    SqlDialect sqlDialect = sqlContext.getSqlDialect();
    if (queryCall instanceof Query.QueryField field) {
      return toFullColumnQuoteString(sqlContext, field.getModelName(), field.getFieldName());
    } else if (queryCall instanceof Query.QueryFunc func) {
      List<String> arguments = new ArrayList<>();
      for (Object arg : func.getArgs()) {
        if (arg instanceof Query.QueryCall callArg) {
          arguments.add(toSqlCall(sqlContext, callArg));
        } else {
          arguments.add(arg instanceof String str ? "'" + str + "'" : arg.toString());
        }
      }
      return sqlDialect.getFunctionString(func.getOperator(), arguments.toArray(String[]::new));
    } else if (queryCall instanceof Query.QueryValue queryValue) {
      return "'" + queryValue.value() + "'";
    }
    return null;
  }

  private static String toSqlWhereClause(SqlContext sqlContext, String condition) {
    SqlExpressionCalculator conditionCalculator = sqlContext.getConditionCalculator();
    try {
      return conditionCalculator.calculateIncludeValue(condition);
    } catch (ExpressionCalculatorException e) {
      throw new SqlExecutionException("calculate sql where error", e);
    }
  }

  private static SqlClauseResult toSqlWhereClauseWithPrepared(SqlContext sqlContext, String condition) {
    SqlExpressionCalculator conditionCalculator = sqlContext.getConditionCalculator();
    try {
      return conditionCalculator.calculate(condition, null);
    } catch (ExpressionCalculatorException e) {
      throw new SqlExecutionException("calculate sql where error", e);
    }
  }

  private static String toFullColumnQuoteString(SqlContext sqlContext, String modelName, String fieldName) {
    SqlDialect sqlDialect = sqlContext.getSqlDialect();
    PhysicalNamingStrategy physicalNamingStrategy = sqlContext.getPhysicalNamingStrategy();
    if (modelName == null) {
      return sqlDialect.quoteIdentifier(fieldName);
    }
    return sqlDialect.quoteIdentifier(physicalNamingStrategy.toPhysicalTableName(modelName)) + "." + sqlDialect.quoteIdentifier(fieldName);
  }

  private static String toPhysicalTableNameQuoteString(SqlContext sqlContext, String name) {
    SqlDialect sqlDialect = sqlContext.getSqlDialect();
    PhysicalNamingStrategy physicalNamingStrategy = sqlContext.getPhysicalNamingStrategy();
    return sqlDialect.quoteIdentifier(physicalNamingStrategy.toPhysicalTableName(name));
  }

}
