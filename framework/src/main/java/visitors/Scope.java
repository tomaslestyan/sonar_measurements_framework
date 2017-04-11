/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.visitors;

/**
 * Enum of visitor scopes
 * @author Tomas Lestyan
 */
public enum Scope {

	METHOD(2), CLASS(1), ALL(0);

	private final int value;

	/**
	 * Constructor
	 * @param text
	 */
	private Scope(final int value) {
		this.value = value;
	}

	/**
	 * @return the value of the scope
	 */
	public int getValue() {
		return value;
	}
}
