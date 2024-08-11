package tech.wetech.flexmodel.graphql

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.wetech.flexmodel.Field
import tech.wetech.flexmodel.Model
import tech.wetech.flexmodel.RelationField
import tech.wetech.flexmodel.SessionFactory

class GraphQLSchemaProcessor {

  GraphQLSchemaProcessor(SessionFactory sf) {
    this.sf = sf
  }

  private final Logger log = LoggerFactory.getLogger(GraphQLSchemaProcessor.class)

  private final SessionFactory sf

  String graphqlSchemaString

  Map<String, QueryRootInfo> dataFetcherTypes = [:]

  def typeMapping = [
    "id"      : "ID",
    "string"  : "String",
    "text"    : "String",
    "decimal" : "Float",
    "int"     : "Int",
    "bigint"  : "Int",
    "boolean" : "Boolean",
    "datetime": "String",
    "json"    : "String",
  ]

  def comparisonMapping = [
    "id"      : "Int_comparison_exp",
    "string"  : "String_comparison_exp",
    "text"    : "String_comparison_exp",
    "decimal" : "Int_comparison_exp",
    "int"     : "Int_comparison_exp",
    "bigint"  : "Int_comparison_exp",
    "boolean" : "String_comparison_exp",
    "datetime": "String_comparison_exp",
    "json"    : "String_comparison_exp",
    "relation": "String_comparison_exp",
  ]

  void execute() {
    graphqlSchemaString = genSchemaString()
    log.debug("generate graphql schema:\n", graphqlSchemaString);
  }

  String genSelectionField(String schemaName, Field field) {
    if (field instanceof RelationField) {
      return "${field.name} : ${field.cardinality == RelationField.Cardinality.ONE_TO_ONE ? "${schemaName}_${field.targetEntity}" : "[${schemaName}_${field.targetEntity}]"}"
    }
    return "${field.name}: ${typeMapping[field.type]}"
  }

  String genTypeModel(String schemaName, Model model) {
    String key = "${schemaName}_${model.name}"
    return """
type ${key} {
  ${model.fields.collect { field -> genSelectionField(schemaName, field) }.join("\n")}
}

"aggregated selection of \\"${key}\\""
type ${key}_aggregate {
  aggregate: ${key}_aggregate_fields
  nodes: [${key}!]!
}

"aggregate fields of \\"${key}\\""
type ${key}_aggregate_fields {
  count: ${key}
  max: ${key}
  min: ${key}
  sum: ${key}
  avg: ${key}
}
"""
  }

  String genInputModel(String schemaName, Model model) {
    String key = "${schemaName}_${model.name}"
    return """
"Boolean expression to filter rows from the table \\"${schemaName}.${model.name}\\". All fields are combined with a logical 'AND'."
input ${key}_bool_exp {
  _and: [${key}_bool_exp!]
  _or: [${key}_bool_exp!]
${
      model.fields.findAll { field -> field.type != 'relation' }
        .collect { field -> "${field.name}: ${comparisonMapping[field.type]}" }.join("\n")
    }
}

input ${key}_order_by {
 ${
      model.fields.findAll { field -> field.type != 'relation' }
        .collect { field -> "${field.name}: order_by" }.join("\n")
    }
}
"""
  }

  String genEnumModel(String schemaName, Model model) {
    String key = "${schemaName}_${model.name}"
    return """
enum ${key}_select_column {
${
      model.fields.findAll { field -> field.type != 'relation' }
        .collect { field -> "  " + field.name }.join("\n")
    }
}
"""
  }

  String genSchemaString() {
    return """
type Query {
${
      sf.getSchemaNames().collect { schemaName ->
        sf.getModels(schemaName).collect { model ->
          {
            String key = "${schemaName}_${model.name}"
            dataFetcherTypes[key] = new QueryRootInfo(schemaName, model.name, FetchType.QUERY)

            return """
${key}(
  "filter the rows returned"
   where: ${key}_bool_exp
  "sort the rows by one or more columns"
  order_by: [${key}_order_by!]
  "limit the number of rows returned"
  limit: Int
  "skip the first n rows"
  offset: Int
  distinct_on: [${key}_select_column!]
): [${key}!]!

${key}_aggregate(
  "filter the rows returned"
   where: ${key}_bool_exp
  "sort the rows by one or more columns"
  order_by: [${key}_order_by!]
  "limit the number of rows returned"
  limit: Int
  "skip the first n rows"
  offset: Int
  distinct_on: [${key}_select_column!]
): ${key}_aggregate!

${model?.findIdField() ?
              """
${key}_by_pk(
  ${model?.findIdField()?.get()?.name}: ${typeMapping[model?.findIdField()?.get()?.type]}!
): ${key}
""" : ""
            }
"""
          }
        }.join("\n")
      }.join("\n")
    }
}

${
      sf.getSchemaNames().collect { schemaName ->
        sf.getModels(schemaName).collect { model ->
          genTypeModel(schemaName, model) }.join("\n")
      }.join("\n")
    }

type Mutation {
${
      sf.getSchemaNames().collect { schemaName ->
        sf.getModels(schemaName).collect { model ->
          {
            String key = "${schemaName}_${model.name}"
            return """
  "delete data from the table: ${key}"
  delete_${key}(
    "filter the rows which have to be deleted"
    where: ${key}_bool_exp!
  ): mutation_response

"""
          }
        }.join("\n")
      }.join("\n")
    }
}

${
      sf.getSchemaNames().collect { schemaName ->
        sf.getModels(schemaName).collect { model ->
          genInputModel(schemaName, model) }.join("\n")
      }.join("\n")
    }
${
      sf.getSchemaNames().collect { schemaName ->
        sf.getModels(schemaName).collect { model ->
          genEnumModel(schemaName, model) }.join("\n")
      }.join("\n")
    }

"Boolean expression to compare columns of type \\"Int\\". All fields are combined with logical 'AND'."
input Int_comparison_exp {
  _eq: Int
  _gt: Int
  _gte: Int
  _in: [Int!]
  _is_null: Boolean
  _lt: Int
  _lte: Int
  _neq: Int
  _nin: [Int!]
}

"Boolean expression to compare columns of type \\"String\\". All fields are combined with logical 'AND'."
input String_comparison_exp {
  _eq: String
  _gt: String
  _gte: String
  _in: [String!]
  _is_null: Boolean
  _lt: String
  _lte: String
  _neq: String
  _nin: [String!]
  "does the column NOT match the given POSIX regular expression, case insensitive"
_niregex: String
  "does the column NOT match the given POSIX regular expression, case sensitive"
_nregex: String
  "does the column NOT match the given SQL regular expression"
_nsimilar: String
  "does the column NOT match the given case-insensitive pattern"
_nilike: String
  "does the column NOT match the given pattern"
_nlike: String
  "does the column match the given POSIX regular expression, case insensitive"
_iregex: String
  "does the column match the given POSIX regular expression, case sensitive"
_regex: String
  "does the column match the given SQL regular expression"
_similar: String
  "does the column match the given case-insensitive pattern"
_ilike: String
  "does the column match the given pattern"
_like: String
}

"column ordering options"
enum order_by {
    "in ascending order, nulls last"
  asc
    "in descending order, nulls first"
  desc
}

"ordering argument of a cursor"
enum cursor_ordering {
    "ascending ordering of the cursor"
  ASC
    "descending ordering of the cursor"
  DESC
}

type mutation_response {
  "data from the rows affected by the mutation"
  affected_rows: Int!
}

    """
  }
}
