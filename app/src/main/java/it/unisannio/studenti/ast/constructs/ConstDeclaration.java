package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class ConstDeclaration implements ASTNode {
    private String name;
    private int value;

    public ConstDeclaration(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() { return name; }
    public int getValue() { return value; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        return ind + "{ \"type\": \"ConstDeclaration\", \"name\": \"" + name + "\", \"value\": " + value + " }";
    }
}
