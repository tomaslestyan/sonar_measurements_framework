/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api.components;

import java.util.Collection;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.sonar.api.ce.measure.Component;
import org.sonar.plugins.java.api.JavaFileScannerContext;

import main.java.framework.api.Scope;

/**
 * Custom component interface
 * @author Tomas
 */
public interface IComponent {

	/**
	 * @return ID of the sonar PROJECT component
	 */
	String getSonarProjectID();

	/**
	 * @return Key of the file of the component as specified in {@link Component#getKey()}
	 */
	String getFileKey();

	/**
	 * @return Key of the file of the component as specified in {@link JavaFileScannerContext#getFileKey()}
	 */
	String getSonarFileKey();

	/**
	 * @return the unique ID of the component
	 */
	String getID();

	/**
	 * @return the parent class of the component (not super class nor interface) in which it is located, null for regular classes
	 */
	String getParent();

	/**
	 * @return Collection of child components, e.g., nested classes, anonymous classes or methods
	 */
	Collection<IComponent> getChildComponents();

	/**
	 * @return the measures of the component (key: metric, value: measure for the metric)
	 */
	Map<String, Integer> getMeasures();

	/**
	 * Add an additional derived measure. This measure is computed on the side of the host plugin and its value will be not stored in Sonar DB
	 * @param metricID
	 * @param value
	 */
	void addComplexMeasure(String metricID, Integer value);

	/**
	 * @return the startLine
	 */
	public int getStartLine();

	/**
	 * @return the endLine
	 */
	public int getEndLine();

	/**
	 * @return scope of the component e.g,  {@link MethodComponent}  = {@link Scope#METHOD}
	 */
	public Scope getScope();

	/**
	 * Draft of the JSon format
	 * @return JSonRepresentation of the component
	 */
	@SuppressWarnings("unchecked")
	default String getComponentAsJsonString () {
		JSONObject componentJson = new JSONObject();
		JSONArray measuresJson = new JSONArray();
		getMeasures().forEach((k, v) -> {
			JSONObject measureJson = new JSONObject();
			measureJson.put(k, v);
			measuresJson.add(measureJson.toJSONString());
		});
		componentJson.put("id", getID());
		componentJson.put("measures", measuresJson.toJSONString());
		return null;
	}

}
