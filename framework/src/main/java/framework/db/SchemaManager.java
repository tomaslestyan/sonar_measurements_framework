package main.java.framework.db;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ToDo: create javadoc
 *
 * @author Klara Erlebachova
 */
public class SchemaManager {

    /** The logger object */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private HikariDataSource dataSource;

    private static final String CREATE_COMPONENTS = "CREATE TABLE IF NOT EXISTS Measurement_Framework_Components (" +
            "id varchar(255) NOT NULL, " +
            "projectKey varchar(255) NOT NULL, " +
            "fileKey varchar(255) NOT NULL, " +
            "parent varchar(255), " +
            "type int NOT NULL,  " +
            "package varchar(255) NOT NULL, " +
            "superClass varchar(255), " +
            "interfaces varchar(65536) NOT NULL, " +
            "startLine int, " +
            "endLine int, " +
            "PRIMARY KEY (id));";

    private static final String MEASURES_COLUMNS =
            "(id varchar(255) NOT NULL, " +
                    "value int, " +
                    "ComponentsId varchar(255) NOT NULL REFERENCES Measurement_Framework_Components (id), " +
                    "MetricsId varchar(255) NOT NULL REFERENCES Metrics (name), PRIMARY KEY (id));";

    private static final String CREATE_MEASURES = "CREATE TABLE IF NOT EXISTS Measurement_Framework_Measures " + MEASURES_COLUMNS;

    private static final String CREATE_RECENT_MEASURES = "CREATE TABLE IF NOT EXISTS Measurement_Framework_Recent_Measures " + MEASURES_COLUMNS;

    public SchemaManager(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Create mandatory tables in Sonar DB if they were not created yet.
     *
     * @return <code>true</code> if tables was created or the were created before, <code>false</code> otherwise
     */
    public boolean createTables() {
        try (Connection connection = this.dataSource.getConnection()) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(CREATE_COMPONENTS + CREATE_MEASURES + CREATE_RECENT_MEASURES);
            }
        } catch (SQLException e) {
            log.warn("Can't create the plugin tables", e);
            return false;
        }
        return true;
    }

    /**
     * Drop mandatory tables in Sonar DB
     */
    public void dropTables() {
        try (Connection connection = this.dataSource.getConnection()) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(
                        "DROP TABLE  Measurement_Framework_Measures; " +
                                "DROP TABLE Measurement_Framework_Recent_Measures; " +
                                "DROP TABLE Measurement_Framework_Components;"
                );
            }
        } catch (SQLException e) {
            log.warn("Can't drop tables", e);
        }
    }
}
