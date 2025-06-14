package tech.wetech.flexmodel.codegen

import groovy.util.logging.Log

import java.nio.file.Path

/**
 * PojoGenerator Class
 * This class generates Java POJOs based on the model definitions.
 *
 * Author: cjbi
 */
@Log
class DAOGenerator extends AbstractGenerator {

  @Override
  String getTargetFile(GenerationContext context, String targetDirectory) {
    return Path.of(targetDirectory, "dao", context.modelClass.getShortClassName() + "DAO.java").toString()
  }

  @Override
  void writeModel(PrintWriter out, GenerationContext context) {
    def modelClass = context.modelClass
    String rootPackage = context.getVariable("rootPackage");
    def className = "${modelClass.shortClassName}DAO"
    out.println "package ${rootPackage}.dao;"
    out.println ""
    out.println "import jakarta.inject.Inject;"
    out.println "import jakarta.inject.Singleton;"

    out.println "import tech.wetech.flexmodel.JsonObjectConverter;"
    out.println "import tech.wetech.flexmodel.Query;"
    out.println "import tech.wetech.flexmodel.dsl.Predicate;"
    out.println "import tech.wetech.flexmodel.Session;"
    out.println "import tech.wetech.flexmodel.SessionFactory;"
    out.println ""
    out.println "import java.util.List;"
    out.println "import java.util.Map;"
    out.println "import java.util.Set;"
    out.println "import java.util.function.Consumer;"
    out.println "import java.util.function.UnaryOperator;"
    out.println ""
    out.println "import ${modelClass.fullClassName};"
    out.println ""

    out.println "/**"
    out.println " * Data Access Object (DAO) class for managing {@link ${modelClass.shortClassName}} entities."
    out.println " * Provides methods to perform CRUD operations and execute queries on the {@link ${modelClass.shortClassName}} model."
    out.println " * Utilizes the Flexmodel framework for data handling and persistence."
    out.println " * <br/>"
    out.println " * Generated by Flexmodel Generator"
    out.println " */"
    out.println "@SuppressWarnings(\"all\")"
    out.println "@Singleton"
    out.println "public class $className {"
    out.println ""
    out.println "  private final SessionFactory sessionFactory;"
    out.println "  private final JsonObjectConverter jsonObjectConverter;"
    out.println ""
    out.println "  // Schema name used for the database operations"
    out.println "  private final String schemaName = \"${modelClass.schemaName}\";"
    out.println "  // Model name associated with the ${modelClass.shortClassName} data"
    out.println "  private final String modelName = \"${modelClass.original.name}\";"
    out.println ""
    out.println "  // Injected session factory for creating sessions with the database"
    out.println "  @Inject"
    out.println "  public $className(SessionFactory sessionFactory) {"
    out.println "    this.sessionFactory = sessionFactory;"
    out.println "    this.jsonObjectConverter = sessionFactory.getJsonObjectConverter();"
    out.println "  }"

    if (modelClass.idField) {
      out.println "  // Field name used for identifying records"
      out.println "  private final String idFieldName = \"${modelClass.idField.original.name}\";"
      out.println ""
      out.println "  /**"
      out.println "   * Updates a {@link ${modelClass.shortClassName}} record identified by its ID."
      out.println "   *"
      out.println "   * @param record The ${modelClass.shortClassName} record containing the updated values."
      out.println "   * @param id The ID of the record to be updated."
      out.println "   * @param <S> The type parameter extending ${modelClass.shortClassName}."
      out.println "   * @return The number of rows affected by the update operation."
      out.println "   */"
      out.println "  public <S extends ${modelClass.shortClassName}> int updateById(S record, ${modelClass.idField.shortTypeName} id) {"
      out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
      out.println "      return session.updateById(modelName, record, id);"
      out.println "    }"
      out.println "  }"
      out.println ""

      out.println "  /**"
      out.println "   * Updates a {@link ${modelClass.shortClassName}} record identified by its ID."
      out.println "   *"
      out.println "   * @param record The ${modelClass.shortClassName} record containing the updated values."
      out.println "   * @param id The ID of the record to be updated."
      out.println "   * @param <S> The type parameter extending ${modelClass.shortClassName}."
      out.println "   * @return The number of rows affected by the update operation."
      out.println "   */"
      out.println "  public <S extends ${modelClass.shortClassName}> int updateIgnoreNullById(S record, ${modelClass.idField.shortTypeName} id) {"
      out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
      out.println "      return session.updateById(modelName, record, id);"
      out.println "    }"
      out.println "  }"
      out.println ""

      out.println "  /**"
      out.println "   * Checks if a record with the specified ID exists."
      out.println "   *"
      out.println "   * @param id The ID of the record to check."
      out.println "   * @return {@code true} if a record with the given ID exists; {@code false} otherwise."
      out.println "   */"
      out.println "  public boolean existsById(${modelClass.idField.shortTypeName} id) {"
      out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
      out.println "      // Check existence of the record with the given ID"
      out.println "      return session.existsById(modelName, id);"
      out.println "    }"
      out.println "  }"
      out.println ""

      out.println "  /**"
      out.println "   * Finds a {@link ${modelClass.shortClassName}} record by its ID."
      out.println "   *"
      out.println "   * @param id The ID of the record to find."
      out.println "   * @return The ${modelClass.shortClassName} record with the given ID, or {@code null} if not found."
      out.println "   */"
      out.println "  public ${modelClass.shortClassName} findById(${modelClass.idField.shortTypeName} id) {"
      out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
      out.println "      // Find the record with the given ID"
      out.println "      return session.findById(modelName, id, ${modelClass.shortClassName}.class);"
      out.println "    }"
      out.println "  }"
      out.println ""

      out.println "  /**"
      out.println "   * Finds a {@link ${modelClass.shortClassName}} record by its ID with a/anoption for nested query fetching."
      out.println "   *"
      out.println "   * @param id The ID of the record to find."
      out.println "   * @param nested query Whether to perform a nested query fetch."
      out.println "   * @return The ${modelClass.shortClassName} record with the given ID, or {@code null} if not found."
      out.println "   */"
      out.println "  public ${modelClass.shortClassName} findById(${modelClass.idField.shortTypeName} id, boolean nestedQuery) {"
      out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
      out.println "      // Find the record with the given ID and apply nested query fetching if specified"
      out.println "      return session.findById(modelName, id, ${modelClass.shortClassName}.class, nestedQuery);"
      out.println "    }"
      out.println "  }"
      out.println ""

      out.println "  /**"
      out.println "   * Creates a new {@link ${modelClass.shortClassName}} record in the database and provides the generated ID."
      out.println "   *"
      out.println "   * @param record The ${modelClass.shortClassName} record to be created."
      out.println "   * @param idR A callback to handle the generated ID."
      out.println "   * @param <S> The type parameter extending ${modelClass.shortClassName}."
      out.println "   * @return The number of rows affected by the insert operation."
      out.println "   */"
      out.println "  public <S extends ${modelClass.shortClassName}> int create(S record, Consumer<${modelClass.idField.shortTypeName}> idR) {"
      out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
      out.println "      // Convert record to a map and insert it into the database, then pass the generated ID to the callback"
      out.println "      return session.insert(modelName, record, id -> idR.accept((${modelClass.idField.shortTypeName}) id));"
      out.println "    }"
      out.println "  }"
      out.println ""

      out.println "  /**"
      out.println "   * Save a {@link ${modelClass.shortClassName}} record identified by its ID."
      out.println "   *"
      out.println "   * @param record The ${modelClass.shortClassName} record containing the updated values."
      out.println "   * @param id The ID of the record to be updated."
      out.println "   * @param <S> The type parameter extending ${modelClass.shortClassName}."
      out.println "   * @return The number of rows affected by the update operation."
      out.println "   */"
      out.println "  public <S extends ${modelClass.shortClassName}> S save(S record) {"
      out.println "    if (record.get${modelClass.idField.variableName.capitalize()}() != null && findById(record.get${modelClass.idField.variableName.capitalize()}()) != null) {"
      out.println "      updateById(record, record.get${modelClass.idField.variableName.capitalize()}());"
      out.println "    } else {"
      out.println "      create(record, id -> record.set${modelClass.idField.variableName.capitalize()}(id));"
      out.println "    }"
      out.println "    return record;"
      out.println "  }"
      out.println ""

      out.println "  /**"
      out.println "   * delete a {@link ${modelClass.shortClassName}} record by its ID."
      out.println "   *"
      out.println "   * @param id The ID of the record to find."
      out.println "   * @return The ${modelClass.shortClassName} record with the given ID, or {@code null} if not found."
      out.println "   */"
      out.println "  public int deleteById(${modelClass.idField.shortTypeName} id) {"
      out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
      out.println "      // delete the record with the given ID"
      out.println "      return session.deleteById(modelName, id);"
      out.println "    }"
      out.println "  }"
      out.println ""

    }

    out.println ""
    out.println "  /**"
    out.println "   * Creates a new {@link ${modelClass.shortClassName}} record in the database."
    out.println "   *"
    out.println "   * @param record The ${modelClass.shortClassName} record to be created."
    out.println "   * @param <S> The type parameter extending ${modelClass.shortClassName}."
    out.println "   * @return The number of rows affected by the insert operation."
    out.println "   */"
    out.println "  public <S extends ${modelClass.shortClassName}> int create(S record) {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      // Convert record to a map and insert it into the database"
    out.println "      return session.insert(modelName, record);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Updates existing {@link ${modelClass.shortClassName}} records that match the specified criteria."
    out.println "   *"
    out.println "   * @param record The ${modelClass.shortClassName} record containing the updated values."
    out.println "   * @param unaryOperator A function to apply the update criteria."
    out.println "   * @param <S> The type parameter extending ${modelClass.shortClassName}."
    out.println "   * @return The number of rows affected by the update operation."
    out.println "   */"
    out.println "  public <S extends ${modelClass.shortClassName}> int update(S record, Predicate predicate) {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      return session.update(modelName, record, predicate);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Updates existing {@link ${modelClass.shortClassName}} records that match the specified criteria."
    out.println "   *"
    out.println "   * @param record The ${modelClass.shortClassName} record containing the updated values."
    out.println "   * @param unaryOperator A function to apply the update criteria."
    out.println "   * @param <S> The type parameter extending ${modelClass.shortClassName}."
    out.println "   * @return The number of rows affected by the update operation."
    out.println "   */"
    out.println "  public <S extends ${modelClass.shortClassName}> int updateIgnoreNull(S record, Predicate predicate) {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      return session.update(modelName, record, predicate);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Deletes records matching the specified criteria."
    out.println "   *"
    out.println "   * @param unaryOperator A function to apply the delete criteria."
    out.println "   * @return The number of rows affected by the delete operation."
    out.println "   */"
    out.println "  public int delete(Predicate predicate) {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      // Delete records matching the criteria"
    out.println "      return session.delete(modelName, predicate);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Deletes all {@link ${modelClass.shortClassName}} records in the database."
    out.println "   *"
    out.println "   * @return The number of rows affected by the delete operation."
    out.println "   */"
    out.println "  public int deleteAll() {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      // Delete all records for the model"
    out.println "      return session.deleteAll(modelName);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Checks if records matching the specified query criteria exist."
    out.println "   *"
    out.println "   * @param queryUnaryOperator A function to apply the query criteria."
    out.println "   * @return {@code true} if records matching the criteria exist; {@code false} otherwise."
    out.println "   */"
    out.println "  public boolean exists(UnaryOperator<Query> queryUnaryOperator) {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      // Check existence of records matching the query criteria"
    out.println "      return session.exists(modelName, queryUnaryOperator);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Checks if records matching the specified query criteria exist."
    out.println "   *"
    out.println "   * @param predicate A function to apply the query criteria."
    out.println "   * @return {@code true} if records matching the criteria exist; {@code false} otherwise."
    out.println "   */"
    out.println "  public boolean exists(Predicate predicate) {"
    out.println "    return exists(q-> q.withFilter(predicate));"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Counts the number of records matching the specified query criteria."
    out.println "   *"
    out.println "   * @param queryUnaryOperator A function to apply the query criteria."
    out.println "   * @return The number of records matching the criteria."
    out.println "   */"
    out.println "  public long count(UnaryOperator<Query> queryUnaryOperator) {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      // Count records matching the query criteria"
    out.println "      return session.count(modelName, queryUnaryOperator);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Counts the number of records matching the specified query criteria."
    out.println "   *"
    out.println "   * @param predicate A function to apply the query criteria."
    out.println "   * @return The number of records matching the criteria."
    out.println "   */"
    out.println "  public long count(Predicate predicate) {"
    out.println "    return count(q-> q.withFilter(predicate));"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Finds records matching the specified query criteria."
    out.println "   *"
    out.println "   * @param queryUnaryOperator A function to apply the query criteria."
    out.println "   * @return A list of {@link ${modelClass.shortClassName}} records matching the criteria."
    out.println "   */"
    out.println "  public List<${modelClass.shortClassName}> find(UnaryOperator<Query> queryUnaryOperator) {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      // Find records matching the query criteria"
    out.println "      return session.find(modelName, queryUnaryOperator, ${modelClass.shortClassName}.class);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Finds records matching the specified query criteria."
    out.println "   *"
    out.println "   * @param predicate A function to apply the query criteria."
    out.println "   * @return A list of {@link ${modelClass.shortClassName}} records matching the criteria."
    out.println "   */"
    out.println "  public List<${modelClass.shortClassName}> find(Predicate predicate) {"
    out.println "    return find(q-> q.withFilter(predicate));"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Finds all records."
    out.println "   *"
    out.println "   * @return A list of {@link ${modelClass.shortClassName}} records matching the criteria."
    out.println "   */"
    out.println "  public List<${modelClass.shortClassName}> findAll() {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      // Find records matching the query criteria"
    out.println "      return session.find(modelName, query-> query, ${modelClass.shortClassName}.class);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Finds records matching the specified query criteria."
    out.println "   *"
    out.println "   * @param queryUnaryOperator A function to apply the query criteria."
    out.println "   * @return A list of {@link ${modelClass.shortClassName}} records matching the criteria."
    out.println "   */"
    out.println "  public <T> List<T> find(UnaryOperator<Query> queryUnaryOperator, Class<T> resultType) {"
    out.println "    try (Session session = sessionFactory.createSession(schemaName)) {"
    out.println "      // Find records matching the query criteria"
    out.println "      return session.find(modelName, queryUnaryOperator, resultType);"
    out.println "    }"
    out.println "  }"
    out.println ""

    out.println "  /**"
    out.println "   * Finds records matching the specified query criteria."
    out.println "   *"
    out.println "   * @param predicate A function to apply the query criteria."
    out.println "   * @return A list of {@link ${modelClass.shortClassName}} records matching the criteria."
    out.println "   */"
    out.println "  public <T> List<T> find(Predicate predicate, Class<T> resultType) {"
    out.println "    return find(q-> q.withFilter(predicate), resultType);"
    out.println "  }"
    out.println ""

    out.println "}"
  }

}
