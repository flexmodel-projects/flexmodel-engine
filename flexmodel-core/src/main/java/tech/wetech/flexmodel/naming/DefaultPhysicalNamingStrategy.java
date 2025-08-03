package tech.wetech.flexmodel.naming;

/**
 * @author cjbi
 */
public class DefaultPhysicalNamingStrategy implements PhysicalNamingStrategy {

  @Override
  public String toPhysicalTableName(String tableName) {
    return tableName;
  }

  @Override
  public String toPhysicalSequenceName(String sequenceName) {
    return sequenceName;
  }

}
