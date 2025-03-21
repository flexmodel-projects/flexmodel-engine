package tech.wetech.flexmodel.graphql

import tech.wetech.flexmodel.EnumField
import tech.wetech.flexmodel.RelationField
import tech.wetech.flexmodel.ScalarType
import tech.wetech.flexmodel.TypedField
import tech.wetech.flexmodel.codegen.AbstractModelListGenerator
import tech.wetech.flexmodel.codegen.ModelField
import tech.wetech.flexmodel.codegen.ModelListGenerationContext

/**
 * @author cjbi
 */
class GraphQLSchemaGenerator extends AbstractModelListGenerator {

  def typeMapping = [
    (ScalarType.ID.getType())      : "ID",
    (ScalarType.STRING.getType())  : "String",
    (ScalarType.TEXT.getType())    : "String",
    (ScalarType.FLOAT.getType())   : "Float",
    (ScalarType.INT.getType())     : "Int",
    (ScalarType.LONG.getType())    : "Int",
    (ScalarType.BOOLEAN.getType()) : "Boolean",
    (ScalarType.DATETIME.getType()): "String",
    (ScalarType.DATE.getType())    : "String",
    (ScalarType.TIME.getType())    : "String",
    (ScalarType.JSON.getType())    : "JSON",
  ]

  def comparisonMapping = [
    (ScalarType.ID.getType())      : "Int_comparison_exp",
    (ScalarType.STRING.getType())  : "String_comparison_exp",
    (ScalarType.TEXT.getType())    : "String_comparison_exp",
    (ScalarType.FLOAT.getType())   : "Float_comparison_exp",
    (ScalarType.INT.getType())     : "Int_comparison_exp",
    (ScalarType.LONG.getType())    : "Int_comparison_exp",
    (ScalarType.BOOLEAN.getType()) : "Boolean_comparison_exp",
    (ScalarType.DATETIME.getType()): "Date_comparison_exp",
    (ScalarType.DATE.getType())    : "DateTime_comparison_exp",
    (ScalarType.TIME.getType())    : "DateTime_comparison_exp",
    (ScalarType.JSON.getType())    : "JSON_comparison_exp",
  ]

  def toGraphQLType(ModelField itt, ModelListGenerationContext context) {
    if (itt.isRelationField()) {
      def rf = itt.originalField as RelationField
      if (rf.multiple) {
        return "[${itt.modelClass.schemaName}_${rf.from}]"
      } else {
        return "${itt.modelClass.schemaName}_${rf.from}"
      }
    } else if (itt.isIdentity() || itt.isBasicField()) {
      def f = itt.originalField as TypedField
      return "${typeMapping[f.type]}"
    } else if (itt.isEnumField() && context.modelListClass.containsEnumClass((itt.originalField as EnumField).from)) {
      def ef = itt.originalField as EnumField
      if (ef.multiple) {
        return "[${itt.modelClass.schemaName}_${ef.from}]"
      } else {
        return "${itt.modelClass.schemaName}_${ef.from}"
      }
    } else {
      return "String"
    }
  }

  @Override
  def generate(PrintWriter out, ModelListGenerationContext context) {
    out.println "schema {"
    out.println "  query: Query"
    out.println "  mutation: Mutation"
    out.println "}"

    context.modelListClass.enumList.each {
      out.println ""
      out.println "enum ${it.schemaName}_${it.originalEnum.name} {"
      it.elements.each {
        out.println "  ${it}"
      }
      out.println "}"
    }
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
        out.println "  ${it.variableName} : ${toGraphQLType(it, context)}"
      }
      out.println "  _join: Query"
      out.println "  _join_mutation: Mutation"
      out.println "}"
      out.println ""
      "aggregated selection of \\\"${key}\\\""
      out.println "type ${key}_aggregate {"
      out.println "  _count(distinct: Boolean, field: ${key}_select_field): Int!"
      out.println "  _max: ${key}!"
      out.println "  _min: ${key}!"
      out.println "  _sum: ${key}!"
      out.println "  _avg: ${key}_avg_fields!"
      out.println "  _join: Query"
      out.println "  _join_mutation: Mutation"
      out.println "}"
      out.println ""
      out.println "type ${key}_avg_fields {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.variableName}: Float"
        }
      }
      out.println "}"
      out.println ""
    }
    context.modelListClass.modelList.each {
      out.println ""
      def schemaName = it.schemaName
      def key = "${it.schemaName}_${it.modelName}"
      // gen input model
      out.println "\"Boolean expression to filter rows from the table \\\"${key}\\\". All fields are combined with a logical 'AND'.\""
      out.println "input ${key}_bool_exp {"
      out.println "  _and: [${key}_bool_exp!]"
      out.println "  _or: [${key}_bool_exp!]"
      it.allFields.each {
        if (!it.isRelationField() && !it.isEnumField()) {
          TypedField f = it.originalField as TypedField
          out.println "  ${it.variableName}: ${comparisonMapping[f.type]}"
        } else if (it.isEnumField() && context.modelListClass.containsEnumClass((it.originalField as EnumField).from)) {
          EnumField enumField = it.originalField as EnumField
          out.println "  ${it.variableName}: ${schemaName}_${enumField.from}_comparison_exp"
        } else {
          out.println "  ${it.variableName}: String_comparison_exp"
        }
      }
      out.println "}"
      out.println ""
      out.println "\"Ordering options when selecting data from \\\"${key}\\\".\"\n"
      out.println "input ${key}_order_by {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.variableName}: order_by"
        }
      }
      out.println "}"
      out.println ""
      out.println "input ${key}_insert_input {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.variableName}: ${toGraphQLType(it, context)}"
        }
      }
      out.println "}"
      out.println ""
      out.println "input ${key}_set_input {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.variableName}: ${toGraphQLType(it, context)}"
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
          out.println "  ${it.variableName}"
        }
      }
      out.println "}"
    }

    out.println ""
    out.println "directive @internal on VARIABLE_DEFINITION"
    out.println "directive @export(as: String) on FIELD"
    out.println "directive @transform(get: String!) on FIELD"
    out.println ""
    out.println "scalar JSON"
    out.println "scalar DateTime"
    out.println "scalar Date"
    out.println "scalar Time"
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
    out.println "\"Boolean expression to compare fields of type \\\"Time\\\". All fields are combined with logical 'AND.'\""
    out.println "input Time_comparison_exp {"
    out.println "  _eq: Time"
    out.println "  _ne: Time"
    out.println "  _gt: Time"
    out.println "  _lt: Time"
    out.println "  _gte: Time"
    out.println "  _lte: Time"
    out.println "  _in: [Time!]"
    out.println "  _nin: [Time!]"
    out.println "  _between: [Time!]"
    out.println "}"
    out.println ""
    context.modelListClass.enumList.each {
      out.println ""
      out.println "\"Boolean expression to compare fields of type \\\"DateTime\\\". All fields are combined with logical 'AND.'\""
      out.println "input ${it.schemaName}_${it.originalEnum.name}_comparison_exp {"
      out.println "  _eq: ${it.schemaName}_${it.originalEnum.name}"
      out.println "  _ne: ${it.schemaName}_${it.originalEnum.name}"
      out.println "  _in: [${it.schemaName}_${it.originalEnum.name}!]"
      out.println "  _nin: [${it.schemaName}_${it.originalEnum.name}!]"
      out.println "}"
      out.println ""
    }
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
    out.println "  _join: Query"
    out.println "  _join_mutation: Mutation"
    out.println "}"

  }

}
