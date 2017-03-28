/**
 * 
 */
package main.java.components;

import java.util.Collection;

import main.java.db.SonarDbClient;

/**
 * @author Tomas
 *
 */
public class ComponentFactory {

	public static Collection<IComponent> getComponents() {
		SonarDbClient client = new SonarDbClient(true);
		Collection<IComponent> components = client.getComponents();
		client.disconnect();
		return components;
	}
}
