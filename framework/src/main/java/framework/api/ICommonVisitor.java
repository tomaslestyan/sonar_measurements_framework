/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api;

/**
 * Common Visitor interface for framework metrics
 * @author Tomas
 */
public interface ICommonVisitor {

	/**
	 * @return {@link Language} enum 
	 */
	Language getLanguage();

	/**
	 * @return the associated metric key
	 */
	public abstract String getKey();

	/**
	 * @return {@link Scope} enum
	 */
	public abstract Scope getScope();

	/**
	 * @return result of computation
	 */
	public abstract int getResult();

}
