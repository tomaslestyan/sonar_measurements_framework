package main.java.framework.java.measurement;

import org.sonar.plugins.java.api.tree.*;

import java.util.List;

public class MethodReturnVisitor extends BaseTreeVisitor {

    private String packageName;
    private String identifier = "";
    private List<String> imports;

    public MethodReturnVisitor(String packageName, List<String> imports){
        this.packageName = packageName;
        this.imports = imports;
    }

    @Override
    public void visitClass(ClassTree tree) {
        identifier = MeasurementUtils.getClassName(tree, packageName);
    }

    @Override
    public void visitMethod(MethodTree tree) {
        if (tree.returnType() == null){
            if (tree.parent() instanceof ClassTree) {
                tree.parent().accept(this);
            }
        } else {
            tree.returnType().accept(this);
        }
    }

    @Override
    public void visitPrimitiveType(PrimitiveTypeTree tree) {
        identifier = tree.symbolType().name();
    }

    @Override
    public void visitIdentifier(IdentifierTree tree) {
        if (!identifier.isEmpty()){
            identifier += ".";
        }
        for (String importSymbol : imports) {
            if ((importSymbol != null) && importSymbol.endsWith(tree.name())) {
                identifier += importSymbol;
                return;
            }
        }
        identifier += tree.name();
        super.visitIdentifier(tree);
    }

    public String getResult(){
        return identifier;
    }
}
