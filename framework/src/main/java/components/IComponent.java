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
public interface IComponent {

	String getSonarComponentID();

	String getID();

	Collection<IComponent> getChildComponents();

	Map<Object, Object> getMeasures();

}
