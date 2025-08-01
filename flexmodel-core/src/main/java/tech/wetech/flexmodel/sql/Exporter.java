package tech.wetech.flexmodel.sql;

/**
 * Defines a contract for exporting of database objects (tables, sequences, etc) for use in SQL {@code CREATE} and
 * {@code DROP} scripts.
 *
 * @author cjbi@outlook.com
 */
public interface Exporter<T extends Exportable> {

  String[] NO_COMMANDS = new String[0];

  /**
   * Get the commands needed for creation.
   *
   * @param exportable
   * @return The commands needed for creation scripting.
   */
  String[] getSqlCreateString(T exportable);

  /**
   * Get the commands needed for dropping.
   *
   * @param exportable
   * @return The commands needed for drop scripting.
   */
  String[] getSqlDropString(T exportable);

}
