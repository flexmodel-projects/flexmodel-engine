package tech.wetech.flexmodel.containers;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @author cjbi
 */
public class GBaseContainer<SELF extends MySQLContainer<SELF>> extends JdbcDatabaseContainer<SELF> {

  public static final String NAME = "gbase";
  private String databaseName;
  private String username;
  private String password;
  public static final int GBASE_PORT = 9088;

  public GBaseContainer() {
    this("liaosnet/gbase8s:v8.8_3503x1_x64");
  }

  public GBaseContainer(String dockerImageName) {
    super(DockerImageName.parse(dockerImageName));
    this.databaseName = "testdb";
    this.username = "gbasedbt";
    this.password = "GBase123$%";
    addExposedPort(GBASE_PORT);
  }

  @Override
  public String getDriverClassName() {
    return "com.gbasedbt.jdbc.Driver";
  }

  @Override
  public String getJdbcUrl() {
    return "jdbc:gbasedbt-sqli://" + this.getHost() + ":"
           + this.getFirstMappedPort() + "/" + databaseName + ":GBASEDBTSERVER=gbase01;DELIMIDENT=y;";
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
    return "select count(*) from systables";
  }
}
