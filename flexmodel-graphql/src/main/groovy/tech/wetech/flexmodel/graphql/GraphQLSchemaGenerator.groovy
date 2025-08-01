package tech.wetech.flexmodel.graphql

import tech.wetech.flexmodel.codegen.AbstractGenerator
import tech.wetech.flexmodel.codegen.GenerationContext
import tech.wetech.flexmodel.codegen.ModelField
import tech.wetech.flexmodel.model.field.EnumField
import tech.wetech.flexmodel.model.field.RelationField
import tech.wetech.flexmodel.model.field.ScalarType
import tech.wetech.flexmodel.model.field.TypedField

/**
 * @author cjbi
 */
class GraphQLSchemaGenerator extends AbstractGenerator {

  def typeMapping = [
    (ScalarType.STRING.getType())  : "String",
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
    (ScalarType.STRING.getType())  : "String_comparison_exp",
    (ScalarType.FLOAT.getType())   : "Float_comparison_exp",
    (ScalarType.INT.getType())     : "Int_comparison_exp",
    (ScalarType.LONG.getType())    : "Int_comparison_exp",
    (ScalarType.BOOLEAN.getType()) : "Boolean_comparison_exp",
    (ScalarType.DATETIME.getType()): "Date_comparison_exp",
    (ScalarType.DATE.getType())    : "DateTime_comparison_exp",
    (ScalarType.TIME.getType())    : "DateTime_comparison_exp",
    (ScalarType.JSON.getType())    : "JSON_comparison_exp",
  ]

  I18nUtil i18n = new I18nUtil()

  def toGraphQLType(ModelField itt, GenerationContext context) {
    if (itt.isRelationField()) {
      def rf = itt.original as RelationField
      if (rf.multiple) {
        return "[${itt.modelClass.schemaName}_${rf.from}]"
      } else {
        return "${itt.modelClass.schemaName}_${rf.from}"
      }
    } else if (itt.isBasicField()) {
      def f = itt.original as TypedField
      if (itt.isIdentity()) {
        return "ID"
      }
      return "${typeMapping[f.type]}"
    } else if (itt.isEnumField() && context.containsEnumClass((itt.original as EnumField).from)) {
      def ef = itt.original as EnumField
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
  void write(PrintWriter out, GenerationContext context) {
    out.println "schema {"
    out.println "  query: Query"
    out.println "  mutation: Mutation"
    out.println "}"

    context.enumClassList.each {
      out.println ""
      out.println "enum ${it.schemaName}_${it.original.name} {"
      it.elements.each {
        out.println "  ${it}"
      }
      out.println "}"
    }
    out.println ""
    // gen query
    out.println "\"${i18n.getString("gql.query.comment")}\""
    out.println "type Query {"
    context.modelClassList.each {
      def key = "${it.schemaName}_${it.original.name}"
      out.println ""
      out.println " \"${i18n.getString("gql.query.find.comment", it.schemaName, it.original.name)}\""
      out.println "  ${DataFetchers.FIND.keyFunc.apply(it.schemaName, it.original.name)}("
      out.println "    \"${i18n.getString("gql.query.where.comment")}\""
      out.println "    where: ${key}_bool_exp"
      out.println "    \"${i18n.getString("gql.query.order_by.comment")}\""
      out.println "    order_by: ${key}_order_by"
      out.println "    \"${i18n.getString("gql.query.size.comment")}\""
      out.println "    size: Int"
      out.println "    \"${i18n.getString("gql.query.page.comment")}\""
      out.println "    page: Int"
      out.println "  ): [${key}!]!"
      out.println ""
      out.println "  \"${i18n.getString("gql.query.aggregate.comment", it.schemaName, it.original.name)}\""
      out.println "  ${DataFetchers.AGGREGATE.keyFunc.apply(it.schemaName, it.original.name)}("
      out.println "    \"${i18n.getString("gql.query.where.comment")}\""
      out.println "    where: ${key}_bool_exp"
      out.println "    \"${i18n.getString("gql.query.order_by.comment")}\""
      out.println "    order_by: ${key}_order_by"
      out.println "    \"${i18n.getString("gql.query.size.comment")}\""
      out.println "    size: Int"
      out.println "    \"${i18n.getString("gql.query.page.comment")}\""
      out.println "    page: Int"
      out.println "  ): ${key}_aggregate!"
      out.println ""
      out.println "  \"${i18n.getString("gql.query.find_one.comment", it.schemaName, it.original.name)}\""
      out.println "  ${DataFetchers.FIND_ONE.keyFunc.apply(it.schemaName, it.original.name)}("
      out.println "    \"filter the rows returned\""
      out.println "    where: ${key}_bool_exp"
      out.println "  ): ${key}"
    }
    out.println "}"
    // gen mutation
    out.println ""
    out.println "\"${i18n.getString("gql.mutation.comment")}\""
    out.println "type Mutation {"
    context.modelClassList.each {
      def key = "${it.schemaName}_${it.original.name}"
      out.println ""
      out.println "  \"${i18n.getString("gql.mutation.delete.comment", it.schemaName, it.original.name)}\""
      out.println "  ${DataFetchers.MUTATION_DELETE.keyFunc.apply(it.schemaName, it.original.name)}("
      out.println "    \"${i18n.getString("gql.mutation.delete.filter.comment")}\""
      out.println "    where: ${key}_bool_exp!"
      out.println "  ): mutation_response"
      if (it.idField) {
        out.println ""
        out.println "  \"${i18n.getString("gql.mutation.delete_by_id.comment", it.schemaName, it.original.name)}\""
        out.println "  ${DataFetchers.MUTATION_DELETE_BY_ID.keyFunc.apply(it.schemaName, it.original.name)}("
        out.println "    id: ID!"
        out.println "  ): ${key}"
        out.println ""
        out.println "  \"${i18n.getString("gql.mutation.update_by_id.comment", it.schemaName, it.original.name)}\""
        out.println "  ${DataFetchers.MUTATION_UPDATE_BY_ID.keyFunc.apply(it.schemaName, it.original.name)}("
        out.println "   _set: ${key}_set_input"
        out.println "   id: ID!"
        out.println "  ): ${key}"
      }
      out.println ""
      out.println "  \"${i18n.getString("gql.mutation.create.comment", it.schemaName, it.original.name)}\""
      out.println "  ${DataFetchers.MUTATION_CREATE.keyFunc.apply(it.schemaName, it.original.name)}("
      out.println "    data: ${key}_insert_input"
      out.println "  ): ${key}"
      out.println ""
      out.println "  \"${i18n.getString("gql.mutation.update.comment", it.schemaName, it.original.name)}\""
      out.println "  ${DataFetchers.MUTATION_UPDATE.keyFunc.apply(it.schemaName, it.original.name)}("
      out.println "    _set: ${key}_set_input"
      out.println "    where: ${key}_bool_exp!"
      out.println "  ): mutation_response"
    }
    out.println "}"
    out.println ""
    context.modelClassList.each {
      def key = "${it.schemaName}_${it.original.name}"
      out.println "  \"${i18n.getString("gql.type.model.comment", it.schemaName, it.original.name)}\""
      out.println "type ${key} {"
      it.allFields.each {
        out.println "  ${it.name} : ${toGraphQLType(it, context)}"
      }
      out.println "  _join: Query"
      out.println "  _join_mutation: Mutation"
      out.println "}"
      out.println ""
      "\"${i18n.getString("gql.query.aggregate.selection.comment", key)}\""
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
          out.println "  ${it.name}: Float"
        }
      }
      out.println "}"
      out.println ""
    }
    context.modelClassList.each {
      out.println ""
      def schemaName = it.schemaName
      def key = "${it.schemaName}_${it.original.name}"
      // gen input model
      out.println "\"${i18n.getString("gql.bool_expr.comment", key)}\""
      out.println "input ${key}_bool_exp {"
      out.println "  _and: [${key}_bool_exp!]"
      out.println "  _or: [${key}_bool_exp!]"
      it.allFields.each {
        if (!it.isRelationField() && !it.isEnumField()) {
          TypedField f = it.original as TypedField
          out.println "  ${it.name}: ${comparisonMapping[f.type]}"
        } else if (it.isEnumField() && context.containsEnumClass((it.original as EnumField).from)) {
          EnumField enumField = it.original as EnumField
          out.println "  ${it.name}: ${schemaName}_${enumField.from}_comparison_exp"
        } else {
          out.println "  ${it.name}: String_comparison_exp"
        }
      }
      out.println "}"
      out.println ""
      out.println "\"${i18n.getString("gql.order_by.comment", key)}\""
      out.println "input ${key}_order_by {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.name}: order_by"
        }
      }
      out.println "}"
      out.println ""
      out.println "input ${key}_insert_input {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.name}: ${toGraphQLType(it, context)}"
        }
      }
      out.println "}"
      out.println ""
      out.println "input ${key}_set_input {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.name}: ${toGraphQLType(it, context)}"
        }
      }
      out.println "}"
    }
    context.modelClassList.each {
      def key = "${it.schemaName}_${it.original.name}"
      out.println ""
      out.println "enum ${key}_select_field {"
      it.allFields.each {
        if (!it.isRelationField()) {
          out.println "  ${it.name}"
        }
      }
      out.println "}"
    }

    out.println ""
    out.println "\"${i18n.getString("gql.directive.internal.comment")}\""
    out.println "directive @internal on VARIABLE_DEFINITION"
    out.println "\"${i18n.getString("gql.directive.export.comment")}\""
    out.println "directive @export(as: String) on FIELD"
    out.println "\"${i18n.getString("gql.directive.transform.comment")}\""
    out.println "directive @transform(get: String!) on FIELD"
    out.println ""
    out.println "scalar JSON"
    out.println "scalar DateTime"
    out.println "scalar Date"
    out.println "scalar Time"
    out.println ""
    out.println "\"${i18n.getString("gql.comparison_exp.comment", "Int")}\""
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
    out.println "\"${i18n.getString("gql.comparison_exp.comment", "Float")}\""
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
    out.println "\"${i18n.getString("gql.comparison_exp.comment", "String")}\""
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
    out.println "\"${i18n.getString("gql.comparison_exp.comment", "JSON")}\""
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
    out.println "\"${i18n.getString("gql.comparison_exp.comment", "Boolean")}\""
    out.println "input Boolean_comparison_exp {"
    out.println "  _eq: Boolean"
    out.println "  _ne: Boolean"
    out.println "}"
    out.println ""
    out.println "\"${i18n.getString("gql.comparison_exp.comment", "Date")}\""
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
    out.println "\"${i18n.getString("gql.comparison_exp.comment", "DateTime")}\""
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
    out.println "\"${i18n.getString("gql.comparison_exp.comment", "Time")}\""
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
    context.enumClassList.each {
      out.println ""
      out.println "\"${i18n.getString("gql.comparison_exp.comment", "Enum")}\""
      out.println "input ${it.schemaName}_${it.original.name}_comparison_exp {"
      out.println "  _eq: ${it.schemaName}_${it.original.name}"
      out.println "  _ne: ${it.schemaName}_${it.original.name}"
      out.println "  _in: [${it.schemaName}_${it.original.name}!]"
      out.println "  _nin: [${it.schemaName}_${it.original.name}!]"
      out.println "}"
      out.println ""
    }
    out.println ""
    out.println "\"${i18n.getString("gql.enum.order_by.comment")}\""
    out.println "enum order_by {"
    out.println "    \"${i18n.getString("gql.enum.order_by.asc.comment")}\""
    out.println "  asc"
    out.println "    \"${i18n.getString("gql.enum.order_by.desc.comment")}\""
    out.println "  desc"
    out.println "}"
    out.println ""
    out.println "\"${i18n.getString("gql.mutation.response.comment")}\""
    out.println "type mutation_response {"
    out.println "  \"${i18n.getString("gql.mutation.response.affected_rows.comment")}\""
    out.println "  affected_rows: Int!"
    out.println "  _join: Query"
    out.println "  _join_mutation: Mutation"
    out.println "}"

  }

}
