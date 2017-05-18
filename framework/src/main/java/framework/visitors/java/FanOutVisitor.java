package main.java.framework.visitors.java;

import main.java.framework.api.Scope;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Filip Čekovský (433588)
 * @version 18.05.2017
 */

public class FanOutVisitor extends AVisitor {
    private DistinctCallsVisitor distinctCallsVisitor;

    @Override
    public String getKey() {
        return "fanout";
    }

    @Override
    public Scope getScope() {
        return Scope.CLASS;
    }

    @Override
    public void scanTree(Tree tree){
        distinctCallsVisitor = new DistinctCallsVisitor();
        super.scanTree(tree);
    }

    @Override
    public void visitMethod(MethodTree tree){
        distinctCallsVisitor.scanTree(tree);
    }

    @Override
    public int getResult(){
        if(distinctCallsVisitor != null) {
            Collection<String[]> methods = distinctCallsVisitor.getEncounteredMethods();
            Set<String> calledClasses = new HashSet<>();

            for (String[] credentials : methods) {
                if (!credentials[1].equals(credentials[3])) {
                    calledClasses.add(credentials[1]);
                }
            }

            return calledClasses.size();
        } else {
            return 0;
        }
    }
}
