package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class CallStatement implements Statement {
    private String procedureName;

    public CallStatement(String procedureName) {
        this.procedureName = procedureName;
    }

    public String getProcedureName() { return procedureName; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        return ind + "{ \"type\": \"CallStatement\", \"procedure\": \"" + procedureName + "\" }";
    }
}
