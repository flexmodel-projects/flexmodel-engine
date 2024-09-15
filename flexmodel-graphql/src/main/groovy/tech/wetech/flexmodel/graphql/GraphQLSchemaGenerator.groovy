package tech.wetech.flexmodel.graphql


import tech.wetech.flexmodel.RelationField
import tech.wetech.flexmodel.TypedField
import tech.wetech.flexmodel.codegen.AbstractModelListGenerator
import tech.wetech.flexmodel.codegen.ModelField
import tech.wetech.flexmodel.codegen.ModelListGenerationContext

/**
 * @author cjbi
 */
class GraphQLSchemaGenerator extends AbstractModelListGenerator {

  def typeMapping = [
    "id"      : "ID",
    "string"  : "String",
    "text"    : "Text",
    "decimal" : "Float",
    "int"     : "Int",
    "bigint"  : "Long",
    "boolean" : "Boolean",
    "datetime": "DateTime",
    "date"    : "Date",
    "json"    : "JSON",
  ]

  def comparisonMapping = [
    "id"      : "Int_comparison_exp",
    "string"  : "String_comparison_exp",
    "text"    : "String_comparison_exp",
    "decimal" : "Int_comparison_exp",
    "int"     : "Int_comparison_exp",
    "bigint"  : "Int_comparison_exp",
    "boolean" : "String_comparison_exp",
    "date": "String_comparison_exp",
    "datetime": "String_comparison_exp",
    "json"    : "String_comparison_exp",
    "relation": "String_comparison_exp",
  ]

  def toGraphQLType(ModelField itt) {
    if (itt.isRelationField()) {
      def rf = itt.originalField as RelationField
      if (rf.cardinality == RelationField.Cardinality.ONE_TO_ONE) {
        return "${itt.modelClass.schemaName}_${rf.targetEntity}"
      } else {
        return "[${itt.modelClass.schemaName}_${rf.targetEntity}]"
      }
    } else {
      def f = itt.originalField as TypedField
      return "${typeMapping[f.type]}"
    }
  }

  @Override
  def generate(PrintWriter out, ModelListGenerationContext context) {
    out.println "schema {"
    out.println "  query: Query"
    out.println "  mutation: Mutation"
    out.println "}"
    out.println ""
    // gen query
    out.println "type Query {"
    context.modelListClass.modelList.each {
      def key = "${it.schemaName}_${it.modelName}"
      out.println ""
      out.println "  ${DataFetchers.FIND.keyFunc.apply(it.schemaName, it.modelName)}("
      out.println "    \"filter the rows returned\""
      out.println "    where: ${key}_bool_exp"
      out.println "    \"sort the rows by one or more columns\""
      out.println "    order_by: ${key}_order_by"
      out.println "    \"Specify the max returned records per page (default to 30)\""
      out.println "    size: Int"
      out.println "    \"The page (aka. offset) of the paginated list (default to 1)\""
      out.println "    page: Int"
      out.println "  ): [${key}!]!"
      out.println ""
      out.println "  ${DataFetchers.AGGREGATE.keyFunc.apply(it.schemaName, it.modelName)}("
      out.println "    \"filter the rows returned\""
      out.println "    where: ${key}_bool_exp"
      out.println "    \"sort the rows by one or more columns\""
      out.println "    order_by: ${key}_order_by"
      out.println "    \"Specify the max returned records per page (default to 30)\""
      out.println "    size: Int"
      out.println "    \"The page (aka. offset) of the paginated list (default to 1)\""
      out.println "    page: Int"
      out.println "  ): ${key}_aggregate!"
      out.println ""
      out.println "  ${DataFetchers.FIND_ONE.keyFunc.apply(it.schemaName, it.modelName)}("
      out.println "    \"filter the rows returned\""
      out.println "    where: ${key}_bool_exp"
      out.println "  ): ${key}"
    }
    out.println "}"
    // gen mutation
    out.println "type Mutation {"
    context.modelListClass.modelList.each {
      def key = "${it.schemaName}_${it.modelName}"
      out.println ""
      out.println "  \"delete data from the table: ${key}\""
      out.println "  ${DataFetchers.MUTATION_DELETE.keyFunc.apply(it.schemaName, it.modelName)}("
      out.println "    \"filter the rows which have to be deleted\""
      out.println "    where: ${key}_bool_exp!"
      out.println "  ): mutation_response"
      if (it.idField) {
        out.println ""
        out.println "  ${DataFetchers.MUTATION_DELETE_BY_ID.keyFunc.apply(it.schemaName, it.modelName)}("
        out.println "    id: ID!"
        out.println "  ): ${key}"
        out.println ""
        out.println "  ${DataFetchers.MUTATION_UPDATE_BY_ID.keyFunc.apply(it.schemaName, it.modelName)}("
        out.println "   _set: ${key}_set_input"
        out.println "   id: ID!"
        out.println "  ): ${key}"
      }
      out.println ""
      out.println "  ${DataFetchers.MUTATION_CREATE.keyFunc.apply(it.schemaName, it.modelName)}("
      out.println "    data: ${key}_insert_input"
      out.println "  ): ${key}"
      out.println ""
      out.println "  ${DataFetchers.MUTATION_UPDATE.keyFunc.apply(it.schemaName, it.modelName)}("
      out.println "    _set: ${key}_set_input"
      out.println "    where: ${key}_bool_exp!"
      out.println "  ): mutation_response"
    }
    out.println "}"
    out.println ""
    context.modelListClass.modelList.each {
      def key = "${it.schemaName}_${it.modelName}"
      out.println "type ${key} {"
      it.allFields.each {
        out.println "  ${it.fieldName} : ${toGraphQLType(it)}"
      }
      out.println "}"
      out.println ""
      "aggregated selection of \\\"${key}\\\""
      out.println "type ${key}_aggregate {"
      out.println "  aggregate: ${key}_aggregate_fields"
      out.println "  nodes: [${key}!]!"
      out.println "}"
      out.println ""
      out.println "\"aggregate fields of \\\"${key}\\\"\""
      out.println "type ${key}_aggregate_fields {"
      out.println "  count: ${key}_select_column"
      out.println "  max: ${key}_select_column"
      out.println "  min: ${key}_select_column"
      out.println "  sum: ${key}_select_column"
      out.println "  avg: ${key}_select_column"
      out.println "}"
    }
    context.modelListClass.modelList.each {
      out.println ""
      def key = "${it.schemaName}_${it.modelName}"
      // gen input model
      out.println "\"Boolean expression to filter rows from the table \\\"${key}\\\". All fields are combined with a logical 'AND'.\""
      out.println "input ${key}_bool_exp {"
      out.println "  _and: [${key}_bool_exp!]"
      out.println "  _or: [${key}_bool_exp!]"
      it.allFields.each {
        if (!it.isRelationField()) {
          TypedField f = it.originalField as TypedField
          out.println "  ${it.fieldName}: ${comparisonMapping[f.type]}"
        }
      }
      out.println "}"
      out.println ""
      out.println "\"Ordering options when selecting data from \\\"${key}\\\".\"\n"
      out.println "input ${key}_order_by {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.fieldName}: order_by"
        }
      }
      out.println "}"
      out.println ""
      out.println "input ${key}_insert_input {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.fieldName}: ${toGraphQLType(it)}"
        }
      }
      out.println "}"
      out.println ""
      out.println "input ${key}_set_input {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.fieldName}: ${toGraphQLType(it)}"
        }
      }
      out.println "}"
    }
    context.modelListClass.modelList.each {
      def key = "${it.schemaName}_${it.modelName}"
      out.println ""
      out.println "enum ${key}_select_column {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.fieldName}"
        }
      }
      out.println "}"
    }

    out.println ""
    out.println "scalar JSON"
    out.println "scalar Date"
    out.println "scalar DateTime"
    out.println "scalar Text"
    out.println "scalar Long"
    out.println ""
    out.println "\"Boolean expression to compare columns of type \\\"Int\\\". All fields are combined with logical 'AND.'\""
    out.println "input Int_comparison_exp {"
    out.println "  _eq: Int"
    out.println "  _gt: Int"
    out.println "  _gte: Int"
    out.println "  _in: [Int!]"
    out.println "  _is_null: Boolean"
    out.println "  _lt: Int"
    out.println "  _lte: Int"
    out.println "  _neq: Int"
    out.println "  _nin: [Int!]"
    out.println "}"
    out.println ""
    out.println "\"Boolean expression to compare columns of type \\\"String\\\". All fields are combined with logical 'AND.'\""
    out.println "input String_comparison_exp {"
    out.println "  _eq: String"
    out.println "  _gt: String"
    out.println "  _gte: String"
    out.println "  _in: [String!]"
    out.println "  _is_null: Boolean"
    out.println "  _lt: String"
    out.println "  _lte: String"
    out.println "  _neq: String"
    out.println "  _nin: [String!]"
    out.println "  \"does the column NOT match the given POSIX regular expression, case insensitive\""
    out.println "  _niregex: String"
    out.println "  \"does the column NOT match the given POSIX regular expression, case sensitive\""
    out.println "  _nregex: String"
    out.println "  \"does the column NOT match the given SQL regular expression\""
    out.println "  _nsimilar: String"
    out.println "  \"does the column NOT match the given case-insensitive pattern\""
    out.println "  _nilike: String"
    out.println "  \"does the column NOT match the given pattern\""
    out.println "  _nlike: String"
    out.println "  \"does the column match the given POSIX regular expression, case insensitive\""
    out.println "  _iregex: String"
    out.println "  \"does the column match the given POSIX regular expression, case sensitive\""
    out.println "  _regex: String"
    out.println "  \"does the column match the given SQL regular expression\""
    out.println "  _similar: String"
    out.println "  \"does the column match the given case-insensitive pattern\""
    out.println "  _ilike: String"
    out.println "  \"does the column match the given pattern\""
    out.println "  _like: String"
    out.println "}"
    out.println ""
    out.println "\"column ordering options\""
    out.println "enum order_by {"
    out.println "    \"in ascending order, nulls last\""
    out.println "  asc"
    out.println "    \"in descending order, nulls first\""
    out.println "  desc"
    out.println "}"
    out.println ""
    out.println "\"ordering argument of a cursor\""
    out.println "enum cursor_ordering {"
    out.println "    \"ascending ordering of the cursor\""
    out.println "  ASC"
    out.println "    \"descending ordering of the cursor\""
    out.println "  DESC"
    out.println "}"
    out.println ""
    out.println "type mutation_response {"
    out.println "  \"data from the rows affected by the mutation\""
    out.println "  affected_rows: Int!"
    out.println "}"

  }

}
