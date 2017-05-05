package main.java.framework.db;

/**
 * ToDo: create javadoc
 *
 * @author Klara Erlebachova
 */
public class Configuration {

    public static final Configuration INSTANCE = new Configuration();


    /** URL of SonarQube DB */
    private String connectionString = "jdbc:h2:tcp://localhost:9092/sonar";
    /** JDBC driver used */
    private String jdbcDriver = "org.h2.Driver";
    /** User name used for connection */
    private String dbUser = "";
    /** Password for DB_USER */
    private String dbPassword = "";

    public Configuration() {}

    public Configuration(String connectionString, String jdbcDriver, String dbUser, String dbPassword) {
        this.connectionString = connectionString;
        this.jdbcDriver = jdbcDriver;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public String getConnectionString() {
        return this.connectionString;
    }

    public boolean verifyJdbcDriver() {
        try {
            Class.forName(this.jdbcDriver);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public String getDbUser() {
        return this.dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}
