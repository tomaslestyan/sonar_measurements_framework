/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.example;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.MeasureComputer;

import main.java.components.ComponentFactory;
import main.java.components.IComponent;

/**
 * Example framework usage.
 * Warning not called from anywhere
 * Only for demonstrate how to use the measurement framework plugin
 * @author Tomas
 */
public class ExampleMeasureComputer  implements MeasureComputer {

	/** Collection of components */
	private Collection<IComponent> components;

	/**
	 * Constructor
	 * @param components
	 */
	public ExampleMeasureComputer() {
		this.components = ComponentFactory.getComponents();
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.ce.measure.MeasureComputer#define(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerDefinitionContext)
	 */
	@Override
	public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
		return defContext.newDefinitionBuilder()
				// Output metrics must contains at least one metric
				.setOutputMetrics("ncloc")
				.build();
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.ce.measure.MeasureComputer#compute(org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext)
	 */
	@Override
	public void compute(MeasureComputerContext context) {
		Component sonarComponent = context.getComponent();
		if (sonarComponent.getType().equals(Component.Type.FILE)) {
			List<IComponent> componentsOfScannedFile = components.stream()
					.filter(x -> sonarComponent.getKey().contains(x.getSonarComponentID())) // TODO sonar component key is not aligned with component sonar key yet
					.collect(Collectors.toList());
			for (IComponent comp : componentsOfScannedFile) {
				comp.getMeasures();
				// do something with measures
			}
		}
	}

}
