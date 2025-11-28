package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;
import java.util.List;

public class WriteStatement implements Statement {
    private List<Expression> expressions;

    public WriteStatement(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public List<Expression> getExpressions() { return expressions; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(ind).append("{\n");
        sb.append(ind).append("  \"type\": \"WriteStatement\",\n");
        sb.append(ind).append("  \"expressions\": [\n");
        for (int i = 0; i < expressions.size(); i++) {
            sb.append(expressions.get(i).toJson(indent + 2));
            if (i < expressions.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(ind).append("  ]\n");
        sb.append(ind).append("}");
        return sb.toString();
    }
}
