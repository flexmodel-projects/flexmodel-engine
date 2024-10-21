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
    "text"    : "String",
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
    "decimal" : "Float_comparison_exp",
    "int"     : "Int_comparison_exp",
    "bigint"  : "Long_comparison_exp",
    "boolean" : "Boolean_comparison_exp",
    "date"    : "Date_comparison_exp",
    "datetime": "DateTime_comparison_exp",
    "json"    : "JSON_comparison_exp",
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
      out.println "    \"sort the rows by one or more fields\""
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
      out.println "    \"sort the rows by one or more fields\""
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
      out.println "  _count(distinct: Boolean, field: ${key}_select_field): Int!"
      out.println "  _max: ${key}!"
      out.println "  _min: ${key}!"
      out.println "  _sum: ${key}!"
      out.println "  _avg: ${key}_avg_fields!"
      out.println "}"
      out.println ""
      out.println "type ${key}_avg_fields {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.fieldName}: Float"
        }
      }
      out.println "}"
      out.println ""
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
      out.println "enum ${key}_select_field {"
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
    out.println "scalar Long"
    out.println ""
    out.println "\"Boolean expression to compare fields of type \\\"ID\\\". All fields are combined with logical 'AND.'\""
    out.println "input ID_comparison_exp {"
    out.println "  _eq: String"
    out.println "  _ne: String"
    out.println "  _in: [String!]"
    out.println "  _nin: [String!]"
    out.println "  _contains: String"
    out.println "  _not_contains: String"
    out.println "  _starts_with: String"
    out.println "  _ends_with: String"
    out.println "}"
    out.println ""
    out.println "\"Boolean expression to compare fields of type \\\"Int\\\". All fields are combined with logical 'AND.'\""
    out.println "input Int_comparison_exp {"
    out.println "  _eq: Int"
    out.println "  _ne: Int"
    out.println "  _gt: Int"
    out.println "  _lt: Int"
    out.println "  _gte: Int"
    out.println "  _lte: Int"
    out.println "  _in: [Int!]"
    out.println "  _nin: [Int!]"
    out.println "}"
    out.println ""
    out.println "\"Boolean expression to compare fields of type \\\"Long\\\". All fields are combined with logical 'AND.'\""
    out.println "input Long_comparison_exp {"
    out.println "  _eq: Long"
    out.println "  _ne: Long"
    out.println "  _gt: Long"
    out.println "  _lt: Long"
    out.println "  _gte: Long"
    out.println "  _lte: Long"
    out.println "  _in: [Long!]"
    out.println "  _nin: [Long!]"
    out.println "}"
    out.println ""
    out.println "\"Boolean expression to compare fields of type \\\"Float\\\". All fields are combined with logical 'AND.'\""
    out.println "input Float_comparison_exp {"
    out.println "  _eq: Float"
    out.println "  _ne: Float"
    out.println "  _gt: Float"
    out.println "  _lt: Float"
    out.println "  _gte: Float"
    out.println "  _lte: Float"
    out.println "  _in: [Float!]"
    out.println "  _nin: [Float!]"
    out.println "}"
    out.println ""
    out.println "\"Boolean expression to compare fields of type \\\"String\\\". All fields are combined with logical 'AND.'\""
    out.println "input String_comparison_exp {"
    out.println "  _eq: String"
    out.println "  _ne: String"
    out.println "  _in: [String!]"
    out.println "  _nin: [String!]"
    out.println "  _contains: String"
    out.println "  _not_contains: String"
    out.println "  _starts_with: String"
    out.println "  _ends_with: String"
    out.println "}"
    out.println ""
    out.println "\"Boolean expression to compare fields of type \\\"JSON\\\". All fields are combined with logical 'AND.'\""
    out.println "input JSON_comparison_exp {"
    out.println "  _eq: String"
    out.println "  _ne: String"
    out.println "  _in: [String!]"
    out.println "  _nin: [String!]"
    out.println "  _contains: String"
    out.println "  _not_contains: String"
    out.println "  _starts_with: String"
    out.println "  _ends_with: String"
    out.println "}"
    out.println ""
    out.println "\"Boolean expression to compare fields of type \\\"Boolean\\\". All fields are combined with logical 'AND.'\""
    out.println "input Boolean_comparison_exp {"
    out.println "  _eq: Boolean"
    out.println "  _ne: Boolean"
    out.println "}"
    out.println ""
    out.println "\"Boolean expression to compare fields of type \\\"Date\\\". All fields are combined with logical 'AND.'\""
    out.println "input Date_comparison_exp {"
    out.println "  _eq: Date"
    out.println "  _ne: Date"
    out.println "  _gt: Date"
    out.println "  _lt: Date"
    out.println "  _gte: Date"
    out.println "  _lte: Date"
    out.println "  _in: [Date!]"
    out.println "  _nin: [Date!]"
    out.println "  _between: [Date!]"
    out.println "}"
    out.println ""
    out.println "\"Boolean expression to compare fields of type \\\"DateTime\\\". All fields are combined with logical 'AND.'\""
    out.println "input DateTime_comparison_exp {"
    out.println "  _eq: DateTime"
    out.println "  _ne: DateTime"
    out.println "  _gt: DateTime"
    out.println "  _lt: DateTime"
    out.println "  _gte: DateTime"
    out.println "  _lte: DateTime"
    out.println "  _in: [DateTime!]"
    out.println "  _nin: [DateTime!]"
    out.println "  _between: [DateTime!]"
    out.println "}"
    out.println ""
    out.println "\"field ordering options\""
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
