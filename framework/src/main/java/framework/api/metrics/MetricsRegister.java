/**
 * The MIT License (MIT)
 * Copyright (c) 2016 FI MUNI
 */
package main.java.framework.api.metrics;

import static java.util.Arrays.*;

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
import main.java.framework.java.metricvisitors.AtfdClassVisitor;
import main.java.framework.java.metricvisitors.ClassLinesOfCodeVisitor;
import main.java.framework.java.metricvisitors.ComplexityVisitor;
import main.java.framework.java.metricvisitors.DataAccessVisitor;
import main.java.framework.java.metricvisitors.DistinctCallsVisitor;
import main.java.framework.java.metricvisitors.FanOutVisitor;
import main.java.framework.java.metricvisitors.ForeignDataProvidersVisitor;
import main.java.framework.java.metricvisitors.LinesOfCodeVisitor;
import main.java.framework.java.metricvisitors.LocalityOfAttributesVisitor;
import main.java.framework.java.metricvisitors.MaxNestingVisitor;
import main.java.framework.java.metricvisitors.NumberOfAttributesVisitor;
import main.java.framework.java.metricvisitors.NumberOfMethodsVisitor;
import main.java.framework.java.metricvisitors.TightClassCohesionVisitor;
import main.java.framework.java.metricvisitors.VariableVisitor;
import main.java.framework.java.metricvisitors.WeightedMethodCountVisitor;

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

	public static final Metric<Integer> NOA = new Metric.Builder("noa", "noa", Metric.ValueType.INT)
			.setDescription("Number of attributes for the class")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	public static final Metric<Integer> NOM = new Metric.Builder("nom", "nom", Metric.ValueType.INT)
			.setDescription("Number of methods for the class")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	public static final Metric<Integer> ATFD = new Metric.Builder("atfd", "atfd", Metric.ValueType.INT)
			.setDescription("Access To Foreign Data")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();
	public static final Metric<Integer> ATFD_CLASS = new Metric.Builder("atfd_class", "atfd_class", Metric.ValueType.INT)
			.setDescription("Access To Foreign Data for Classes")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();
	public static final Metric<Integer> LAA = new Metric.Builder("laa", "laa", Metric.ValueType.INT)
			.setDescription("Locality of Attribute Accesses")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();
	public static final Metric<Integer> FDP = new Metric.Builder("fdp", "fdp", Metric.ValueType.INT)
			.setDescription("Foreign Data Providers")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	public static final Metric<Integer> TCC = new Metric.Builder("tcc", "tcc", Metric.ValueType.INT)
			.setDescription("Number of tight class cohesion pairs")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	public static final Metric<Integer> WMC = new Metric.Builder("wmc", "wmc", Metric.ValueType.INT)
			.setDescription("Weighted metod count computed by their cyclomatic complexity")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	// This metric is computed by measure computer and therefor is not accessible until server side analysis is done
	public static final Metric<Integer> CYCLO_AVERAGE = new Metric.Builder("cyclo_average", "cyclo_average", Metric.ValueType.INT)
			.setDescription("Average cyclomatic complexity for class")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	// This metric is computed by measure computer and therefor is not accessible until server side analysis is done
	public static final Metric<Integer> CYCLO_TOTAL = new Metric.Builder("cyclo_total", "cyclo_total", Metric.ValueType.INT)
			.setDescription("Total cyclomatic complexity for class")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	// This metric is computed by measure computer and therefor is not accessible until server side analysis is done
	public static final Metric<Integer> CYCLO_MAXIMUM = new Metric.Builder("cyclo_maximum", "cyclo_maximum", Metric.ValueType.INT)
			.setDescription("Maximum cyclomatic complexity for class")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	public static final Metric<Integer> FALSE_METRIC = new Metric.Builder("none", "none", Metric.ValueType.INT)
			.setDescription("False metric for use in measure computer")
			.setQualitative(false)
			.setDomain(CoreMetrics.DOMAIN_GENERAL)
			.create();

	private static final Map<Metric<? extends Serializable> , Collection<ICommonVisitor>> metricVisitors = ImmutableMap.<Metric<? extends Serializable> , Collection<ICommonVisitor>> builder()
			.put(LOC, Arrays.asList(new LinesOfCodeVisitor()))
			.put(LOC_CLASS, Arrays.asList(new ClassLinesOfCodeVisitor()))
			.put(NOAV, Arrays.asList(new VariableVisitor()))
			.put(CYCLO, Arrays.asList(new ComplexityVisitor()))
			.put(MAXNESTING, Arrays.asList(new MaxNestingVisitor()))
			.put(NOA, Arrays.asList(new NumberOfAttributesVisitor()))
			.put(NOM, Arrays.asList(new NumberOfMethodsVisitor()))
			.put(ATFD, Arrays.asList(new DataAccessVisitor(true)))
			.put(LAA, Arrays.asList(new LocalityOfAttributesVisitor()))
			.put(FDP, Arrays.asList(new ForeignDataProvidersVisitor()))
			.put(WMC, Arrays.asList(new WeightedMethodCountVisitor()))
			.put(TCC, Arrays.asList(new TightClassCohesionVisitor()))
			.put(ATFD_CLASS, Arrays.asList(new AtfdClassVisitor()))
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
	 * @return all metrics computed by measurement framework
	 */
	public static final List<Metric> getFrameworkMetrics() {
		return asList(LOC, LOC_CLASS, NOAV, CYCLO, MAXNESTING, NOA, NOM, LAA, ATFD, ATFD_CLASS,
				FDP, WMC, TCC, CYCLO_AVERAGE, CYCLO_MAXIMUM, CYCLO_TOTAL,
				FALSE_METRIC);
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
