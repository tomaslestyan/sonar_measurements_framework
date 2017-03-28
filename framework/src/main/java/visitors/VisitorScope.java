/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Tomas Lestyan
 */
package main.java.visitors;

/**
 * Enum of visitor scopes
 * @author Tomas Lestyan
 */
public enum VisitorScope {

	METHOD("METHOD"), CLASS("CLASS"), ALL("ALL");

	private final String value;

	/**
	 * Constructor
	 * @param text
	 */
	private VisitorScope(final String value) {
		this.value = value;
	}

	/**
	 * @return the value of the scope
	 */
	public String getValue() {
	        return value;
	    }

	 /**
	  * Get {@link VisitorScope} value from given {@link String} value
	 * @param value
	 * @return
	 */
	public static VisitorScope getScope(String value) {
	        for(VisitorScope scope : values())
	            if (scope.getValue().equalsIgnoreCase(value)) {
	            	return scope;
	            }
	        throw new IllegalArgumentException();
	    }

	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return value;
	}
}
