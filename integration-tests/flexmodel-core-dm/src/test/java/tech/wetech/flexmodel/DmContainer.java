package tech.wetech.flexmodel;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @author cjbi
 */
public class DmContainer<SELF extends DmContainer<SELF>> extends JdbcDatabaseContainer<SELF> {

  public static final String NAME = "dm";
  private String databaseName;
  private String username;
  private String password;
  public static final int DM_PORT = 5236;

  public DmContainer() {
    this("sizx/dm8:1-2-128-22.08.04-166351-20005-CTM");
  }

  public DmContainer(String dockerImageName) {
    super(DockerImageName.parse(dockerImageName));
    this.databaseName = "SYSDBA";
    this.username = "SYSDBA";
    this.password = "SYSDBA001";
    addExposedPort(DM_PORT);
  }

  @Override
  public String getDriverClassName() {
    return "dm.jdbc.driver.DmDriver";
  }

  @Override
  public String getJdbcUrl() {
    String additionalUrlParams = this.constructUrlParameters("?", "&");
    return "jdbc:dm://" + this.getHost() + ":"
           + this.getFirstMappedPort() + "/" + databaseName + additionalUrlParams;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  public SELF withDatabaseName(String databaseName) {
    this.databaseName = databaseName;
    return this.self();
  }

  public SELF withUsername(String username) {
    this.username = username;
    return this.self();
  }

  public SELF withPassword(String password) {
    this.password = password;
    return this.self();
  }

  @Override
  protected String getTestQueryString() {
    return "select 1 from dual";
  }
}
