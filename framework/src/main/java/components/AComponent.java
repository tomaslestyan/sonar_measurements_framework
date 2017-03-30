/**
 * 
 */
package main.java.components;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * @author Tomas
 *
 */
public abstract class AComponent implements IComponent {

	private String id;
	private String sonarComponentID;
	private String parentClass;
	private Collection<IComponent> children;
	private Map<Object, Object> measures = new HashMap<>();

	/**
	 * Constructor
	 * @param id
	 * @param sonarComponentID
	 * @param parentClass
	 * @param children
	 * @param measures
	 */
	protected AComponent(String id, String sonarComponentID, String parentClass, Collection<IComponent> children,
			Map<Object, Object> measures) {
		this.id = id;
		this.sonarComponentID = sonarComponentID;
		this.parentClass = parentClass;
		this.children = children;
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
		return Collections.unmodifiableCollection(children);
	}

	/* (non-Javadoc)
	 * @see main.java.components.IComponent#getMeasures()
	 */
	@Override
	public Map<Object, Object> getMeasures() {
		return measures;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((children == null) ? 0 : children.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((parentClass == null) ? 0 : parentClass.hashCode());
		result = prime * result + ((sonarComponentID == null) ? 0 : sonarComponentID.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AComponent other = (AComponent) obj;
		return  Objects.equal(children, other.children) 
				&& Objects.equal(id, other.id) 
				&& Objects.equal(parentClass, other.parentClass) 
				&& Objects.equal(sonarComponentID, other.sonarComponentID) 
				&& Objects.equal(parentClass, other.parentClass);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass()).add("id", id).toString();
	}
}
