package main.java.framework.java.metricvisitors;

import main.java.framework.api.Scope;
import main.java.framework.api.metrics.CallCredentials;
import org.sonar.plugins.java.api.tree.*;
import java.util.*;

/**
 * @author Filip Čekovský (433588)
 * @version 18.05.2017
 */

public class DistinctCallsVisitor  extends AVisitor{
    private String visitedClass;
    private String visitedMethod;
    private Map<String, String> variables = new HashMap<>();
    private Map<String, String> attributes = new HashMap<>();
    private Set<CallCredentials> encounteredMethods = new HashSet<>();

    @Override
    public String getKey() {
        return "calls";
    }

    @Override
    public Scope getScope() {
        return Scope.CLASS;
    }

    @Override
    public void visitClass(ClassTree classTree){
            visitedClass = classTree.symbol().name();
            super.visitClass(classTree);
            //Todo delete - just for debug
            classTree.closeBraceToken();
    }

    @Override
    public void visitMethod(MethodTree methodTree) {
        visitedMethod = methodTree.symbol().name();

        for (VariableTree parameter: methodTree.parameters()) {
            String typeName = parameter.type().toString();
            variables.put(parameter.simpleName().name(), typeName);
        }

        super.visitMethod(methodTree);
    }


    @Override
    public void visitVariable(VariableTree variableTree){
        String variableType = "";
        if (variableTree.type() instanceof MemberSelectExpressionTree){
            variableType = extractExpression((MemberSelectExpressionTree) variableTree.type()) +
                ((MemberSelectExpressionTree)variableTree.type()).identifier().name();
        } else {
            variableType += variableTree.type().toString();
        }

        if (variableTree.parent() instanceof ClassTree){
            attributes.put(variableTree.simpleName().name(), variableType);
        } else {
            variables.put(variableTree.simpleName().name(), variableType);
        }

        super.visitVariable(variableTree);
    }

    @Override
    public void visitMethodInvocation(MethodInvocationTree tree){
        super.visitMethodInvocation(tree);
        CallCredentials credentials = new CallCredentials();

        credentials.methodOwnerClass = tree.symbol().owner().name();
        credentials.callingMethod = visitedMethod;
        credentials.callingClass = visitedClass;
        credentials.methodName = tree.methodSelect().lastToken().text();
        if(tree.methodSelect().lastToken() == tree.methodSelect().firstToken()){
            credentials.variableName = "this.";
        } else {
            credentials.variableName = tree.methodSelect().firstToken().text() + ".";
        }

        credentials.variableName += extractExpression(tree);
        super.visitMethodInvocation(tree);
    }

    private boolean checkTypes(ExpressionTree tree){
        if (tree instanceof MethodInvocationTree && ((MethodInvocationTree) tree).methodSelect() instanceof MethodInvocationTree){
            ExpressionTree sub = ((MemberSelectExpressionTree)((MethodInvocationTree) tree).methodSelect()).expression();
            return ((sub instanceof MethodInvocationTree) || (sub instanceof  MemberSelectExpressionTree));
        }
        if (tree instanceof MemberSelectExpressionTree){
            ExpressionTree sub = ((MemberSelectExpressionTree) tree).expression();
            return ((sub instanceof  MethodInvocationTree) || (sub instanceof  MemberSelectExpressionTree));
        }
        return false;
    }

    private String extractExpression(ExpressionTree tree){
        StringBuilder expression = new StringBuilder("");
        try {
            while (checkTypes(tree)) {
                ExpressionTree sub;
                if (tree instanceof MethodInvocationTree) {
                    if(((MethodInvocationTree) tree).methodSelect() instanceof MemberSelectExpressionTree){
                        sub = ((MemberSelectExpressionTree) ((MethodInvocationTree) tree).methodSelect()).expression();
                    } else {
                        return expression.toString();
                    }
                } else {
                    sub = ((MemberSelectExpressionTree) tree).expression();
                }
                if (sub instanceof NewClassTree) {
                    expression.insert(0, ((IdentifierTree) ((NewClassTree) sub).identifier()).name());
                }
                if (sub instanceof MethodInvocationTree) {
                    MethodInvocationTree concreteSub = ((MethodInvocationTree) sub);
                    expression.insert(0, concreteSub.methodSelect().lastToken().text() + "().");

                    tree = ((MemberSelectExpressionTree) concreteSub.methodSelect()).expression();
                } else if (sub instanceof MemberSelectExpressionTree) {
                    MemberSelectExpressionTree concreteSub = ((MemberSelectExpressionTree) sub);
                    expression.insert(0, concreteSub.identifier().name() + ".");
                    tree = concreteSub;
                }
            }
        } catch (Exception e){
            throw e;
        }
        return expression.toString();
    }
}

