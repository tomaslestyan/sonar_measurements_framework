package main.java.plugin;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Plugin;

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
		try {
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/sonar", "sonar", "sonar");
			conn.isValid(1000);
			conn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
		}
	}

	@Override
	public void define(Context context) {
		context.addExtension(MeasurementsSensor.class);
	}
}
