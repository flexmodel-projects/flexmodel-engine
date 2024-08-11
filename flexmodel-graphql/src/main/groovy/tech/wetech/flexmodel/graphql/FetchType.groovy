package tech.wetech.flexmodel.graphql

/**
 * @author cjbi
 */
enum FetchType {
  FIND, AGGREGATE, FIND_BY_ID,
  MUTATION_DELETE, MUTATION_DELETE_BY_ID,
  MUTATION_CREATE,
  MUTATION_UPDATE, MUTATION_UPDATE_BY_ID
}
