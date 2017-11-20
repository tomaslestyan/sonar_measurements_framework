package main.java.framework.java.measurement;

import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.PrimitiveTypeTree;

public class MethodReturnVisitor extends BaseTreeVisitor {

    private String result = "";

    @Override
    public void visitMethod(MethodTree tree) {
        tree.returnType();
        if (tree.returnType() == null){
            //result = this class
        } else {
            tree.returnType().accept(this);
        }
    }

    @Override
    public void visitPrimitiveType(PrimitiveTypeTree tree) {
        result = tree.toString();
    }

    @Override
    public void visitIdentifier(IdentifierTree tree) {
        result += tree.name();
        super.visitIdentifier(tree);
    }

    public String getResult(){
        return result;
    }
}
