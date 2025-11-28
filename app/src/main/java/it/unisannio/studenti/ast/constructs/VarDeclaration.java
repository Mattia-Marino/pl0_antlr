package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class VarDeclaration implements ASTNode {
    private String name;

    public VarDeclaration(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        return ind + "{ \"type\": \"VarDeclaration\", \"name\": \"" + name + "\" }";
    }
}
