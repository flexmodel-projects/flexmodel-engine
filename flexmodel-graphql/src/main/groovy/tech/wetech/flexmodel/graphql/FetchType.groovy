package tech.wetech.flexmodel.graphql

/**
 * @author cjbi
 */
enum FetchType {
  QUERY, QUERY_BY_PK,
  MUTATION_DELETE, MUTATION_DELETE_BY_PK,
  MUTATION_INSERT_ONE,
  MUTATION_UPDATE, MUTATION_UPDATE_BY_PK
}
