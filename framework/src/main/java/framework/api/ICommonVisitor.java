/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api;

/**
 * TODO
 * @author Tomas
 */
public interface ICommonVisitor {

	Language getLanguage();

	public abstract String getKey();

	public abstract Scope getScope();

	public abstract int getResult();

}
