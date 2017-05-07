package main.java.framework.db;

/**
 * Database configuration
 * ToDo: parse Sonarqube confirutaion file
 * ToDo: test with other DBMS
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

    public Configuration() {
    /*  this.connectionString = "jdbc:postgresql://localhost:5432/sonarqube2";
        this.jdbcDriver = "org.postgresql.Driver";
        this.dbUser = "postgres";
        this.dbPassword = "heslo";
     */

    }

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
