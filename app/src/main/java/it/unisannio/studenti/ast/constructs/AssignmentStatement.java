package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class AssignmentStatement implements Statement {
    private String variable;
    private Expression expression;

    public AssignmentStatement(String variable, Expression expression) {
        this.variable = variable;
        this.expression = expression;
    }

    public String getVariable() { return variable; }
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
        sb.append(ind).append("  \"type\": \"AssignmentStatement\",\n");
        sb.append(ind).append("  \"variable\": \"").append(variable).append("\",\n");
        sb.append(ind).append("  \"expression\": ").append(expression.toJson(indent + 1)).append("\n");
        sb.append(ind).append("}");
        return sb.toString();
    }
}
