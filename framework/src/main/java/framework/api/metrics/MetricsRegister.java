/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api.metrics;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.google.common.collect.ImmutableMap;

import main.java.framework.api.ICommonVisitor;
import main.java.framework.api.Language;
import main.java.framework.visitors.java.ClassLinesOfCodeVisitor;
import main.java.framework.visitors.java.ComplexityVisitor;
import main.java.framework.visitors.java.LinesOfCodeVisitor;
import main.java.framework.visitors.java.MaxNestingVisitor;
import main.java.framework.visitors.java.VariableVisitor;

/**
 * Class for register metrics. It registers metric for SonarQube but mainly it just connects the metrics with their visitors.
 * @author Tomas
 */
public class MetricsRegister implements Metrics {

	public static final Metric<Integer> LOC = new Metric.Builder("loc", "loc", Metric.ValueType.INT)
			.setDescription("The number of the lines of the code")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	public static final Metric<Integer> LOC_CLASS = new Metric.Builder("loc_class", "loc_class", Metric.ValueType.INT)
			.setDescription("The number of the lines of the code of the class")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	public static final Metric<Integer> NOAV = new Metric.Builder("noav", "noav", Metric.ValueType.INT)
			.setDescription("Number of Variables")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	public static final Metric<Integer> CYCLO = new Metric.Builder("cyclo", "cyclo", Metric.ValueType.INT)
			.setDescription("Cyclomatic complexity")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	public static final Metric<Integer> MAXNESTING = new Metric.Builder("maxnesting", "maxnesting", Metric.ValueType.INT)
			.setDescription("Maximal Nesting")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	private static final Map<Metric<? extends Serializable> , Collection<ICommonVisitor>> metricVisitors = ImmutableMap.<Metric<? extends Serializable> , Collection<ICommonVisitor>> builder()
			.put(LOC, Arrays.asList(new LinesOfCodeVisitor()))
			.put(LOC_CLASS, Arrays.asList(new ClassLinesOfCodeVisitor()))
			.put(NOAV, Arrays.asList(new VariableVisitor()))
			.put(CYCLO, Arrays.asList(new ComplexityVisitor()))
			.put(MAXNESTING, Arrays.asList(new MaxNestingVisitor()))
			.build();

	/**
	 * @return get visitors of metrics
	 */
	public static final Map<Metric<? extends Serializable> , Collection<ICommonVisitor>> getMetricVisitors() {
		return metricVisitors;
	}

	/**
	 * @param metric 
	 * @param lang 
	 * @return get visitors of metrics
	 */
	public static final ICommonVisitor getMetricVisitorForLanguage(Metric<? extends Serializable> metric, Language lang) {
		Collection<ICommonVisitor> visitors = metricVisitors.get(metric);
		if (visitors == null) {
			return null;
		}
		return visitors.stream().filter(x -> x.getLanguage().equals(lang)).findAny().orElseGet(null);
	}

	/**
	 * @return TODO
	 */
	public static final List<Metric> getFrameworkMetrics() {
		return asList(LOC, LOC_CLASS, NOAV, CYCLO, MAXNESTING);
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.measures.Metrics#getMetrics()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<Metric> getMetrics() {
		return getFrameworkMetrics();
	}
}
