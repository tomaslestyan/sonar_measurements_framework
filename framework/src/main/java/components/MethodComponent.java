/**
 * 
 */
package main.java.components;

import java.util.Map;

/**
 * @author Tomas
 *
 */
public class MethodComponent extends AComponent {

	/**
	 * Constructor
	 * @param id
	 * @param sonarComponentID
	 * @param parentClass
	 * @param measures
	 */
	public MethodComponent(String id, String sonarComponentID, String parentClass, Map<Object, Object> measures) {
		super(id, sonarComponentID, parentClass, null, measures);
	}
}
