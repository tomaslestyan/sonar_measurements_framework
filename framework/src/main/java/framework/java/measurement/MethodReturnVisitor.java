/**
 * The MIT License (MIT)
 * Copyright (c) 2017 FI MUNI
 */
package main.java.framework.java.measurement;

import org.sonar.plugins.java.api.tree.*;

import java.util.List;

/**
 * Class responsible for method's return type retrieval.
 * @author Filip Cekovsky
 */
public class MethodReturnVisitor extends BaseTreeVisitor{

    /** Package of the visited method */
    private String packageName;
    /** Eventual contains fully qualified name of the owner class*/
    private String identifier = "";
    /** Imports present in the class */
    private List<String> imports;

    /**
     * Constructor of the class
     * @param packageName fills {@link #packageName}
     * @param imports fills {@link #imports}
     */
    public MethodReturnVisitor(String packageName, List<String> imports){
        this.packageName = packageName;
        this.imports = imports;
    }

    @Override
    public void visitClass(ClassTree tree){
        identifier = MeasurementUtils.getClassName(tree, packageName);
    }

    @Override
    public void visitMethod(MethodTree tree){
        if (tree.returnType() == null){
            if (tree.parent() instanceof ClassTree){
                tree.parent().accept(this);
            }
        } else {
            tree.returnType().accept(this);
        }
    }

    @Override
    public void visitPrimitiveType(PrimitiveTypeTree tree){
        identifier = tree.symbolType().name();
    }

    @Override
    public void visitIdentifier(IdentifierTree tree){
        if (!identifier.isEmpty()){
            identifier += ".";
        }
        for (String importSymbol : imports) {
            if ((importSymbol != null) && importSymbol.endsWith(tree.name())){
                identifier += importSymbol;
                return;
            }
        }
        identifier += tree.name();
        super.visitIdentifier(tree);
    }

    /**
     * Returns the result of the visit.
     * @return String representing the returned type. In case of type that is imported,
     * it is a fully qualified name of the class.
     */
    public String getResult(){
        return identifier;
    }
}
