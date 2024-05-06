package tech.wetech.flexmodel.containers;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.text.MessageFormat;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * @author cjbi
 */
public class InformixContainer<SELF extends InformixContainer<SELF>> extends JdbcDatabaseContainer<SELF> {
  public static final int INFORMIX_PORT = 9088;
  static final String DEFAULT_USER = "informix";
  static final String DEFAULT_PASSWORD = "in4mix";
  private static final String IFX_CONFIG_DIR = "/opt/ibm/config/";
  private String databaseName = "sysadmin";

  public static final String FULL_IMAGE_NAME = "ibmcom/informix-developer-database";

  public InformixContainer() {
    this(DockerImageName.parse(FULL_IMAGE_NAME + ":latest"));
  }

  public InformixContainer(DockerImageName dockerImageName) {
    super(dockerImageName);
    this.waitStrategy = new LogMessageWaitStrategy()
      .withRegEx(".*Maximum server connections 1.*")
      .withTimes(1)
      .withStartupTimeout(Duration.of(60, SECONDS));
    addExposedPort(INFORMIX_PORT);
  }

  @Override
  protected void configure() {
    addEnv("LICENSE", "accept");
  }

  @Override
  public String getDriverClassName() {
    return "com.informix.jdbc.IfxDriver";
  }

  @Override
  public String getJdbcUrl() {
    String additionalUrlParams = constructUrlParameters(";", ";");
    return MessageFormat.format("jdbc:informix-sqli://{0}:{1}/{2}:INFORMIXSERVER=informix{3}",
      getContainerIpAddress(), String.valueOf(getMappedPort(INFORMIX_PORT)), getDatabaseName(), additionalUrlParams);
  }

  @Override
  public String getUsername() {
    return DEFAULT_USER;
  }

  @Override
  public String getPassword() {
    return DEFAULT_PASSWORD;
  }

  @Override
  protected String getTestQueryString() {
    return "select count(*) from systables";
  }

  @Override
  protected void waitUntilContainerStarted() {
    getWaitStrategy().waitUntilReady(this);
  }

  @Override
  public SELF withDatabaseName(final String databaseName) {
    this.databaseName = databaseName;
    return self();
  }

  @Override
  public String getDatabaseName() {
    return databaseName;
  }

}
