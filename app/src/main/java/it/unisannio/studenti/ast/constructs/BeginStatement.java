package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;
import java.util.List;

public class BeginStatement implements Statement {
    private List<Statement> statements;

    public BeginStatement(List<Statement> statements) {
        this.statements = statements;
    }

    public List<Statement> getStatements() { return statements; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(ind).append("{\n");
        sb.append(ind).append("  \"type\": \"BeginStatement\",\n");
        sb.append(ind).append("  \"statements\": [\n");
        for (int i = 0; i < statements.size(); i++) {
            sb.append(statements.get(i).toJson(indent + 2));
            if (i < statements.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(ind).append("  ]\n");
        sb.append(ind).append("}");
        return sb.toString();
    }
}
