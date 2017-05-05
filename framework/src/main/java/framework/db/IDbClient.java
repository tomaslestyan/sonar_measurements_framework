package main.java.framework.db;

import main.java.framework.api.components.IComponent;
import org.sonar.api.measures.Metric;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * ToDo: create javadoc
 *
 * @author Klara Erlebachova
 */
public interface IDbClient {

    boolean createTables();

    void dropTables();

    boolean saveRecentMeasuresToMeasures();

    void saveComponent(String id, String fileID, String project, String parent, int type, String packageName, String superClass, Collection<String> interfaces, int startLine, int endLine);

    void saveMeasure(Metric<? extends Serializable> metric, String componentID, int value);

    Collection<IComponent> getComponents(String parent);

    List<Integer> getMeasures(String metric);
}
