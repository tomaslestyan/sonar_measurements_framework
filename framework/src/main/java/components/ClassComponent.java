/**
 * 
 */
package main.java.components;

import java.util.Collection;
import java.util.Map;

/**
 * @author Tomas
 *
 */
public class ClassComponent extends AComponent {

	/**
	 * Constructor
	 * @param id
	 * @param sonarComponentID
	 * @param parentClass
	 * @param children
	 * @param measures
	 */
	public ClassComponent(String id, String sonarComponentID, String parentClass, Collection<IComponent> children,
			Map<Object, Object> measures) {
		super(id, sonarComponentID, parentClass, children, measures);
	}
}
