package tech.wetech.flexmodel.sql;

import tech.wetech.flexmodel.*;
import tech.wetech.flexmodel.graph.JoinGraphNode;
import tech.wetech.flexmodel.sql.dialect.SqlDialect;

import java.util.*;

import static tech.wetech.flexmodel.Query.Join.JoinType.INNER_JOIN;
import static tech.wetech.flexmodel.Query.Join.JoinType.LEFT_JOIN;
import static tech.wetech.flexmodel.RelationField.Cardinality.MANY_TO_MANY;

/**
 * @author cjbi
 */
public abstract class BaseSqlStatement {

  protected final SqlContext sqlContext;

  public BaseSqlStatement(SqlContext sqlContext) {
    this.sqlContext = sqlContext;
  }

  public String buildQuerySql(String modelName, Query query) {
    return buildQuerySql(modelName, query, false).first();
  }

  public Pair<String, Map<String, Object>> toQuerySqlWithPrepared(String modelName, Query query) {
    return buildQuerySql(modelName, query, true);
  }

  private Pair<String, Map<String, Object>> buildQuerySql(String modelName, Query query, boolean prepared) {
    QueryHelper.validate(sqlContext, modelName, query);
    Map<String, Object> params = new HashMap<>();
    Model model = sqlContext.getMappedModels().getModel(sqlContext.getSchemaName(), modelName);
    StringBuilder sqlBuilder = new StringBuilder("\nselect ");
    Map<String, String> projectionMap = new HashMap<>();
    appendProjection(modelName, query, model, projectionMap, sqlBuilder);
    appendFromClause(modelName, sqlBuilder);
    appendJoins(modelName, query, model, sqlBuilder, params, prepared);
    appendWhereClause(query, sqlBuilder, params, prepared);
    appendGroupByClause(query, projectionMap, sqlBuilder);
    appendOrderByClause(query, sqlBuilder);
    appendLimitClause(query, sqlBuilder);
    return Pair.of(sqlBuilder.toString(), params);
  }

  private void appendLimitClause(Query query, StringBuilder sqlBuilder) {
    SqlDialect sqlDialect = sqlContext.getSqlDialect();
    if (query.getPage() != null) {
      String limitString = sqlDialect.getLimitString(
        sqlBuilder.toString(),
        Objects.toString(query.getPage().getOffset()),
        Objects.toString(query.getPage().getPageSize())
      );
      sqlBuilder.setLength(0);
      sqlBuilder.append(limitString);
    }
  }

  private void appendOrderByClause(Query query, StringBuilder sqlBuilder) {
    Query.Sort sort = query.getSort();
    if (sort != null) {
      sqlBuilder.append("\norder by ");
      StringJoiner sortColumns = new StringJoiner(", ");
      for (Query.Sort.Order order : sort.getOrders()) {
        sortColumns.add(toFullColumnQuoteString(order.getField().getModelName(), order.getField().getFieldName()) + " " + order.getDirection().name().toLowerCase());
      }
      sqlBuilder.append(sortColumns);
    }
  }

  private void appendGroupByClause(Query query, Map<String, String> projectionMap, StringBuilder sqlBuilder) {
    if (query.getGroupBy() != null) {
      SqlDialect sqlDialect = sqlContext.getSqlDialect();
      sqlBuilder.append("\ngroup by ");
      StringJoiner groupByColumns = new StringJoiner(", ");
      for (Query.QueryField field : query.getGroupBy().getFields()) {
        groupByColumns.add(sqlDialect.supportsGroupByColumnAlias() ? toFullColumnQuoteString(field.getModelName(), field.getFieldName()) : projectionMap.getOrDefault(field.getFieldName(), toFullColumnQuoteString(field.getModelName(), field.getFieldName())));
      }
      sqlBuilder.append(groupByColumns);
    }
  }

  private void appendWhereClause(Query query, StringBuilder sqlBuilder, Map<String, Object> params, boolean prepared) {
    if (query.getFilter() != null) {
      if (prepared) {
        SqlClauseResult sqlClauseResult = toSqlWhereClauseWithPrepared(query.getFilter());
        sqlBuilder.append("\nwhere (").append(sqlClauseResult.sqlClause()).append(")");
        params.putAll(sqlClauseResult.args());
      } else {
        sqlBuilder.append("\nwhere (").append(toSqlWhereClause(query.getFilter())).append(")");
      }
    }
  }

  private void appendJoins(String modelName, Query query, Model model, StringBuilder sqlBuilder, Map<String, Object> params, boolean prepared) {
    Query.Joins joins = query.getJoins();
    if (joins != null) {
      StringBuilder joinCause = new StringBuilder();
      for (Query.Join joiner : joins.getJoins()) {
        String joinTableName = toPhysicalTableNameQuoteString(joiner.getFrom());
        if (joiner.getJoinType() == LEFT_JOIN) {
          joinCause.append("\nleft join ");
        }
        if (joiner.getJoinType() == INNER_JOIN) {
          joinCause.append("\ninner join ");
        }
        String localField = joiner.getLocalField();
        String foreignField = joiner.getForeignField();
        RelationField relationField;
        if (model instanceof Entity entity && (relationField = entity.findRelationByEntityName(joiner.getFrom()).orElse(null)) != null) {
          localField = entity.findIdField().map(IDField::getName).orElseThrow();
          foreignField = relationField.getTargetField();
          if (relationField.getCardinality() == MANY_TO_MANY) {
            Entity targetEntity = (Entity) sqlContext.getMappedModels().getModel(sqlContext.getSchemaName(), relationField.getTargetEntity());
            JoinGraphNode joinGraphNode = new JoinGraphNode(entity, targetEntity, relationField);
            joinCause.append(toPhysicalTableNameQuoteString(joinGraphNode.getJoinName())).append(" \n on \n").append(toFullColumnQuoteString(modelName, localField)).append("=").append(toFullColumnQuoteString(joinGraphNode.getJoinName(), joinGraphNode.getJoinFieldName()));
            if (joiner.getJoinType() == LEFT_JOIN) {
              joinCause.append("\nleft join ");
            }
            if (joiner.getJoinType() == INNER_JOIN) {
              joinCause.append("\ninner join ");
            }
            joinCause.append(joinTableName).append(" \n on \n").append(toFullColumnQuoteString(joiner.getFrom(), foreignField)).append("=").append(toFullColumnQuoteString(joinGraphNode.getJoinName(), joinGraphNode.getInverseJoinFieldName()));
          } else {
            joinCause.append(joinTableName).append(" \n on \n").append(toFullColumnQuoteString(modelName, localField)).append("=").append(toFullColumnQuoteString(joiner.getFrom(), foreignField));
          }
        } else {
          joinCause.append(joinTableName).append(" \n on \n").append(toFullColumnQuoteString(modelName, localField)).append("=").append(toFullColumnQuoteString(joiner.getFrom(), foreignField));
        }
        StringBuilder joinCondition = new StringBuilder();
        if (joiner.getFilter() != null) {
          if (prepared) {
            SqlClauseResult leftSqlWhere = toSqlWhereClauseWithPrepared(joiner.getFilter());
            joinCondition.append(" and ").append(leftSqlWhere.sqlClause());
            params.putAll(leftSqlWhere.args());
          } else {
            joinCondition.append(" and ").append(toSqlWhereClause(joiner.getFilter()));
          }
          joinCause.append(joinCondition);
        }
      }
      sqlBuilder.append(joinCause);
    }
  }

  private void appendFromClause(String modelName, StringBuilder sqlBuilder) {
    String physicalFromTableName = toPhysicalTableNameQuoteString(modelName);
    sqlBuilder.append("\nfrom ").append(physicalFromTableName);
  }

  private void appendProjection(String modelName, Query query, Model model, Map<String, String> projectionMap, StringBuilder sqlBuilder) {
    SqlDialect sqlDialect = sqlContext.getSqlDialect();
    Query.Projection projection = query.getProjection();
    Map<String, RelationField> relationFields = QueryHelper.findRelationFields(model, query);
    StringJoiner columns = new StringJoiner(", ");
    if (projection != null) {
      projection.getFields().forEach((key, value) -> {
        if (!relationFields.containsKey(key)) {
          String sqlCall = toSqlCall(value);
          projectionMap.put(key, sqlCall);
          columns.add("\n " + sqlCall + " " + sqlDialect.quoteIdentifier(key));
        }
      });
    } else {
      model.getFields().forEach(field -> {
        if (!relationFields.containsKey(field.getName())) {
          columns.add("\n " + toFullColumnQuoteString(modelName, field.getName()) + " " + sqlDialect.quoteIdentifier(field.getName()));
          projectionMap.put(field.getName(), field.getName());
        }
      });
    }
    sqlBuilder.append(columns);
  }

  private String toSqlCall(Query.QueryCall queryCall) {
    SqlDialect sqlDialect = sqlContext.getSqlDialect();
    if (queryCall instanceof Query.QueryField field) {
      return toFullColumnQuoteString(field.getModelName(), field.getFieldName());
    } else if (queryCall instanceof Query.QueryFunc func) {
      List<String> arguments = new ArrayList<>();
      for (Object arg : func.getArgs()) {
        if (arg instanceof Query.QueryCall callArg) {
          arguments.add(toSqlCall(callArg));
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

  private String toSqlWhereClause(String condition) {
    SqlExpressionCalculator conditionCalculator = sqlContext.getConditionCalculator();
    try {
      return conditionCalculator.calculateIncludeValue(condition);
    } catch (ExpressionCalculatorException e) {
      throw new SqlExecutionException("Calculate sql where error: " + e.getMessage(), e);
    }
  }

  private SqlClauseResult toSqlWhereClauseWithPrepared(String condition) {
    SqlExpressionCalculator conditionCalculator = sqlContext.getConditionCalculator();
    try {
      return conditionCalculator.calculate(condition, null);
    } catch (ExpressionCalculatorException e) {
      throw new SqlExecutionException("Calculate sql where error: " + e.getMessage(), e);
    }
  }

  private String toFullColumnQuoteString(String modelName, String fieldName) {
    SqlDialect sqlDialect = sqlContext.getSqlDialect();
    PhysicalNamingStrategy physicalNamingStrategy = sqlContext.getPhysicalNamingStrategy();
    if (modelName == null) {
      return sqlDialect.quoteIdentifier(fieldName);
    }
    return sqlDialect.quoteIdentifier(physicalNamingStrategy.toPhysicalTableName(modelName)) + "." + sqlDialect.quoteIdentifier(fieldName);
  }

  private String toPhysicalTableNameQuoteString(String name) {
    SqlDialect sqlDialect = sqlContext.getSqlDialect();
    PhysicalNamingStrategy physicalNamingStrategy = sqlContext.getPhysicalNamingStrategy();
    return sqlDialect.quoteIdentifier(physicalNamingStrategy.toPhysicalTableName(name));
  }

  public record Pair<T, U>(T first, U second) {

    public static <T, U> Pair<T, U> of(T first, U second) {
      return new Pair<>(first, second);
    }
  }


}
