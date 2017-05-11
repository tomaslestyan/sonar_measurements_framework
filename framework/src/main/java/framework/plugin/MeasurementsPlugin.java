/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Plugin;
import org.sonar.api.config.Settings;

import main.java.framework.api.metrics.MetricsRegister;
import main.java.framework.db.Configuration;
import main.java.framework.db.DataSourceProvider;
import main.java.framework.db.SaveMetricsClient;
import main.java.framework.db.SchemaManager;

/**
 * The plugin definition
 * TODO under construction - add each new class
 * @author Tomas Lestyan
 */
public class MeasurementsPlugin implements Plugin {

	/** The logger object */
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	/**
	 * Constructor
	 * @param settings 
	 */
	public MeasurementsPlugin(Settings settings) {
		log.info("plugin created");
		// get database configuration
		String dbUrl = settings.getString("sonar.jdbc.url");
		String driver = settings.getString("sonar.jdbc.driverClassName");
		String userName = settings.getString("sonar.jdbc.username");
		String password = settings.getString("sonar.jdbc.password");
		Configuration configuration = new Configuration(dbUrl, driver, userName, password);
		DataSourceProvider.setConfiguration(configuration);


		// create tables if runs first time
		SchemaManager schemaManager = new SchemaManager(DataSourceProvider.getDataSource());
		//		schemaManager.dropTables(); //DEBUG ONLY
		schemaManager.createTables();


		SaveMetricsClient client = new SaveMetricsClient(DataSourceProvider.getDataSource());
		client.saveRecentMeasuresToMeasures();
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.Plugin#define(org.sonar.api.Plugin.Context)
	 */
	@Override
	public void define(Context context) {
		context.addExtensions(MetricsRegister.class, JavaChecks.class, Rules.class, MeasurementsSensor.class);
	}

}
