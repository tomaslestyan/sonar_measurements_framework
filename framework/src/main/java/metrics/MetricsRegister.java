/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.metrics;

import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import com.google.common.collect.ImmutableMap;

import main.java.visitors.AVisitor;
import main.java.visitors.ComplexityVisitor;
import main.java.visitors.LinesOfCodeVisitor;
import main.java.visitors.MaxNestingVisitor;
import main.java.visitors.VariableVisitor;

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

	private static final Map<Metric<? extends Serializable> , AVisitor> metricVisitors = ImmutableMap.<Metric<? extends Serializable> , AVisitor> builder()
			.put(LOC, new LinesOfCodeVisitor())
			.put(NOAV, new VariableVisitor())
			.put(CYCLO, new ComplexityVisitor())
			.put(MAXNESTING, new MaxNestingVisitor())
			.build();

	/**
	 * @return get visitors of metrics
	 */
	public static final Map<Metric<? extends Serializable> , AVisitor> getMetricVisitors() {
		return metricVisitors;
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.measures.Metrics#getMetrics()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<Metric> getMetrics() {
		return asList(LOC, NOAV, CYCLO, MAXNESTING);
	}
}
