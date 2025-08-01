package tech.wetech.flexmodel.core.naming;

/**
 * @author cjbi
 */
public interface PhysicalNamingStrategy {

  String toPhysicalTableName(String tableName);

  String toPhysicalSequenceName(String sequenceName);

}
