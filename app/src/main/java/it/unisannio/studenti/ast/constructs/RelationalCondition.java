package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class RelationalCondition implements Condition {
    private Expression left;
    private String operator;
    private Expression right;

    public RelationalCondition(Expression left, String operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() { return left; }
    public String getOperator() { return operator; }
    public Expression getRight() { return right; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(ind).append("{\n");
        sb.append(ind).append("  \"type\": \"RelationalCondition\",\n");
        sb.append(ind).append("  \"left\": ").append(left.toJson(indent + 1)).append(",\n");
        sb.append(ind).append("  \"operator\": \"").append(operator).append("\",\n");
        sb.append(ind).append("  \"right\": ").append(right.toJson(indent + 1)).append("\n");
        sb.append(ind).append("}");
        return sb.toString();
    }
}
