package tech.wetech.flexmodel.graphql
/**
 * @author cjbi
 */
record QueryRootInfo(String schemaName,
                     String modelName,
                     FetchType fetchType) {

  String getSchemaName() {
    return schemaName
  }

  String getModelName() {
    return modelName
  }

  FetchType getFetchType() {
    return fetchType
  }
}
