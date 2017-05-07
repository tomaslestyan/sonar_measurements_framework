package main.java.framework.db;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Provides HikariDataSource
 *
 * @author Klara Erlebachova
 */
public class DataSourceProvider {

    private static HikariDataSource hikari = null;
    private static Configuration configuration = null;

    public static final void setConfiguration(Configuration configuration) {
        DataSourceProvider.configuration = configuration;
    }

    public synchronized static final HikariDataSource getDataSource() {
        if (hikari == null) {
            if (DataSourceProvider.configuration == null) {
                throw new RuntimeException("Configuration is not set");
            }
            hikari = createDataSource(DataSourceProvider.configuration);
        }
        return hikari;
    }

    private static final HikariDataSource createDataSource(Configuration configuration) {
        if (!configuration.verifyJdbcDriver()) {
            // log.warn("JDBC driver cannot be found");
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(configuration.getConnectionString());
        dataSource.setUsername(configuration.getDbUser());
        dataSource.setPassword(configuration.getDbPassword());
        return dataSource;
    }

}