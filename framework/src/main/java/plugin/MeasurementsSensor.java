/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.plugin;

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

import main.java.components.ComponentFactory;

/**
 * Sensor for testing purposes
 * @author Tomas
 */
public class MeasurementsSensor implements Sensor {

	/* (non-Javadoc)
	 * @see org.sonar.api.batch.sensor.Sensor#describe(org.sonar.api.batch.sensor.SensorDescriptor)
	 */
	@Override
	public void describe(SensorDescriptor descriptor) {
		descriptor.name("Measurements sensor");
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.batch.sensor.Sensor#execute(org.sonar.api.batch.sensor.SensorContext)
	 */
	@Override
	public void execute(SensorContext context) {
		context.getSonarQubeVersion();
		ComponentFactory.getComponents();
	}

}
