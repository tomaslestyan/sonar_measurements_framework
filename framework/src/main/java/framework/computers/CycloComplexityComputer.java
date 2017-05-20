package main.java.framework.computers;

import main.java.framework.api.metrics.MetricsRegister;
import main.java.framework.db.Configuration;
import main.java.framework.db.DataSourceProvider;
import main.java.framework.db.SaveMetricsClient;
import main.java.framework.db.SonarDbClient;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.MeasureComputer;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;

import java.util.Collection;

/**
 * Computes maximal, average and total cyclomatic complexity for class
 *
 * @author Klara Erlebachova
 */
public class CycloComplexityComputer implements MeasureComputer{

    public static final Metric<Integer> CYCLO_AVERAGE = new Metric.Builder("cyclo_average", "cyclo_average", Metric.ValueType.INT)
            .setDescription("Average cyclomatic complexity for class")
            .setQualitative(false)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();

    public static final Metric<Integer> CYCLO_TOTAL = new Metric.Builder("cyclo_total", "cyclo_total", Metric.ValueType.INT)
            .setDescription("Total cyclomatic complexity for class")
            .setQualitative(false)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();

    public static final Metric<Integer> CYCLO_MAXIMUM = new Metric.Builder("cyclo_maximum", "cyclo_maximum", Metric.ValueType.INT)
            .setDescription("Maximum cyclomatic complexity for class")
            .setQualitative(false)
            .setDomain(CoreMetrics.DOMAIN_GENERAL)
            .create();

    @Override
    public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
        return defContext.newDefinitionBuilder()
                .setOutputMetrics("none")
                .build();
    }

    @Override
    public void compute(MeasureComputerContext context) {
        Component component = context.getComponent();
        if (component.getType() != Component.Type.PROJECT) {
            return;
        }

        DataSourceProvider.setConfiguration(Configuration.INSTANCE);
        SaveMetricsClient saveClient = new SaveMetricsClient(DataSourceProvider.getDataSource());
        SonarDbClient readClient = new SonarDbClient(DataSourceProvider.getDataSource());

        String projectKey = component.getKey();

        Collection<String> classes = readClient.getClassesIdForProject(projectKey);
        for (String classComponent : classes) {
            Collection<Integer> measurements = readClient.getMeasurementsForAllMethodsInClass(
                    classComponent,
                    MetricsRegister.CYCLO.getKey()
            );
            Integer max = 0;
            Integer total = 0;
            for (Integer measurement : measurements) {
                max = Math.max(max, measurement);
                total += measurement;
            }
            Integer average = total / measurements.size();
            saveClient.saveMeasure(CYCLO_AVERAGE, classComponent, average);
            saveClient.saveMeasure(CYCLO_MAXIMUM, classComponent, max);
            saveClient.saveMeasure(CYCLO_TOTAL, classComponent, total);
        }
    }
}
