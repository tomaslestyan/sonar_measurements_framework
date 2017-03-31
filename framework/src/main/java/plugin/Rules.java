/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.plugin;

import java.util.Arrays;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.java.Java;
import org.sonar.squidbridge.annotations.AnnotationBasedRulesDefinition;

/**
 * Rules for satisfy SonarQue mechanisms
 * @author Tomas Lestyan
 */
public class Rules implements RulesDefinition {

	/** The rules repository of the framework */
	public static final String REPOSITORY = "framework";

	/* (non-Javadoc)
	 * @see org.sonar.api.server.rule.RulesDefinition#define(org.sonar.api.server.rule.RulesDefinition.Context)
	 */
	@Override
	public void define(Context context) {
		NewRepository repository = context.createRepository(REPOSITORY, Java.KEY).setName("Framework Rules repository");
		new AnnotationBasedRulesDefinition(repository, Java.KEY).addRuleClasses(false, false, Arrays.asList(JavaChecks.checkClasses()));
		repository.done();
	}

}
