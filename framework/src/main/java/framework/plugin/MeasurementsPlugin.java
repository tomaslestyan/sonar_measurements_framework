/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Plugin;

import main.java.framework.api.metrics.MetricsRegister;
import main.java.framework.db.SonarDbClient;

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
	 */
	public MeasurementsPlugin() {
		log.info("plugin created");
		// create tables if runs first time
		SonarDbClient client = new SonarDbClient(true);
		//		client.dropTables(); //DEBUG ONLY
		client.createTables();
		client.storeMeasures();
		client.disconnect();
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.Plugin#define(org.sonar.api.Plugin.Context)
	 */
	@Override
	public void define(Context context) {
		context.addExtensions(MetricsRegister.class, JavaChecks.class, Rules.class, MeasurementsSensor.class);
	}
}