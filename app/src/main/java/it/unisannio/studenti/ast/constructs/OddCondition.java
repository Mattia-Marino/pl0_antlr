package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class OddCondition implements Condition {
    private Expression expression;

    public OddCondition(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() { return expression; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(ind).append("{\n");
        sb.append(ind).append("  \"type\": \"OddCondition\",\n");
        sb.append(ind).append("  \"expression\": ").append(expression.toJson(indent + 1)).append("\n");
        sb.append(ind).append("}");
        return sb.toString();
    }
}
