package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class NumberLiteral implements Expression {
    private int value;

    public NumberLiteral(int value) {
        this.value = value;
    }

    public int getValue() { return value; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        return ind + "{ \"type\": \"NumberLiteral\", \"value\": " + value + " }";
    }
}
