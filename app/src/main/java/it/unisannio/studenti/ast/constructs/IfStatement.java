package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class IfStatement implements Statement {
    private Condition condition;
    private Statement thenStatement;
    private Statement elseStatement;

    public IfStatement(Condition condition, Statement thenStatement, Statement elseStatement) {
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }

    public Condition getCondition() { return condition; }
    public Statement getThenStatement() { return thenStatement; }
    public Statement getElseStatement() { return elseStatement; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(ind).append("{\n");
        sb.append(ind).append("  \"type\": \"IfStatement\",\n");
        sb.append(ind).append("  \"condition\": ").append(condition.toJson(indent + 1)).append(",\n");
        sb.append(ind).append("  \"then\": ").append(thenStatement.toJson(indent + 1));
        if (elseStatement != null) {
            sb.append(",\n");
            sb.append(ind).append("  \"else\": ").append(elseStatement.toJson(indent + 1)).append("\n");
        } else {
            sb.append("\n");
        }
        sb.append(ind).append("}");
        return sb.toString();
    }
}
