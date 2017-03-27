
package main.java.metrics;

import static java.util.Arrays.asList;

import java.util.List;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

/**
 * @author Tomas
 *
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

	/* (non-Javadoc)
	 * @see org.sonar.api.measures.Metrics#getMetrics()
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public List<Metric> getMetrics() {
		return asList(LOC, NOAV, CYCLO, MAXNESTING);
	}
}
