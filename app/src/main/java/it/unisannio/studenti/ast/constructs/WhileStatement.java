package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class WhileStatement implements Statement {
    private Condition condition;
    private Statement doStatement;

    public WhileStatement(Condition condition, Statement doStatement) {
        this.condition = condition;
        this.doStatement = doStatement;
    }

    public Condition getCondition() { return condition; }
    public Statement getDoStatement() { return doStatement; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(ind).append("{\n");
        sb.append(ind).append("  \"type\": \"WhileStatement\",\n");
        sb.append(ind).append("  \"condition\": ").append(condition.toJson(indent + 1)).append(",\n");
        sb.append(ind).append("  \"do\": ").append(doStatement.toJson(indent + 1)).append("\n");
        sb.append(ind).append("}");
        return sb.toString();
    }
}
