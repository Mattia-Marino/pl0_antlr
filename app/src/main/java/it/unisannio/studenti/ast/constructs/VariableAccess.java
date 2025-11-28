package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class VariableAccess implements Expression {
    private String name;

    public VariableAccess(String name) {
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
        return ind + "{ \"type\": \"VariableAccess\", \"name\": \"" + name + "\" }";
    }
}
