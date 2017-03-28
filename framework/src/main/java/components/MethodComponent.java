/**
 * 
 */
package main.java.components;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tomas
 *
 */
public class MethodComponent implements IComponent {

	private String id;
	private String sonarComponentID;
	private String parentClass;
	private Map<Object, Object> measures = new HashMap<>();


	/**
	 * Constructor
	 * @param id
	 * @param sonarComponentID
	 * @param parentClass
	 * @param measures
	 */
	public MethodComponent(String id, String sonarComponentID, String parentClass, Map<Object, Object> measures) {
		this.id = id;
		this.sonarComponentID = sonarComponentID;
		this.parentClass = parentClass;
		this.measures = measures;
	}

	/* (non-Javadoc)
	 * @see main.java.components.IComponent#getSonarComponentID()
	 */
	@Override
	public String getSonarComponentID() {
		return sonarComponentID;
	}

	/* (non-Javadoc)
	 * @see main.java.components.IComponent#getID()
	 */
	@Override
	public String getID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see main.java.components.IComponent#getChildComponents()
	 */
	@Override
	public Collection<IComponent> getChildComponents() {
		return Collections.emptyList();
	}

	/* (non-Javadoc)
	 * @see main.java.components.IComponent#getMeasures()
	 */
	@Override
	public Map<Object, Object> getMeasures() {
		return measures;
	}

}
