package main.java.framework.computers;

import main.java.framework.api.metrics.MetricsRegister;
import main.java.framework.db.Configuration;
import main.java.framework.db.DataSourceProvider;
import main.java.framework.db.SaveMetricsClient;
import main.java.framework.db.SonarDbClient;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.MeasureComputer;

import java.util.Collection;

/**
 * Computes maximal, average and total cyclomatic complexity for class
 *
 * @author Klara Erlebachova
 */
public class CycloComplexityComputer implements MeasureComputer{

    @Override
    public MeasureComputerDefinition define(MeasureComputerDefinitionContext defContext) {
        return defContext.newDefinitionBuilder()
                .setOutputMetrics(MetricsRegister.FALSE_METRIC.getKey())
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
                    MetricsRegister.CYCLO.getKey(),
                    classComponent
            );
            Integer max = 0;
            Integer total = 0;
            for (Integer measurement : measurements) {
                max = Math.max(max, measurement);
                total += measurement;
            }
            Integer average = measurements.size() != 0 ? total / measurements.size() : 0;
            saveClient.saveMeasure(MetricsRegister.CYCLO_AVERAGE, classComponent, average);
            saveClient.saveMeasure(MetricsRegister.CYCLO_MAXIMUM, classComponent, max);
            saveClient.saveMeasure(MetricsRegister.CYCLO_TOTAL, classComponent, total);
        }
    }
}
