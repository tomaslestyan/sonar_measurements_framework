package main.java.framework.visitors.java;

import main.java.framework.api.Scope;
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
        return Scope.METHOD;
    }

    @Override
    public void scanTree(Tree tree){

        distinctCallsVisitor = new DistinctCallsVisitor();
        distinctCallsVisitor.scanTree(tree);
        super.scanTree(tree);
    }

    @Override
    public int getResult(){
        Collection<DistinctCallsVisitor.Credentials> methods = distinctCallsVisitor.getEncounteredMethods();
        Set<String> calledClasses = new HashSet<>();

        for (DistinctCallsVisitor.Credentials credentials : methods) {
            calledClasses.add(credentials.methodOwnerClass);
        }

        return calledClasses.size();
    }
}
