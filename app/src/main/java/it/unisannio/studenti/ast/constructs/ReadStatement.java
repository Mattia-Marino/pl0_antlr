package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;
import java.util.List;

public class ReadStatement implements Statement {
    private List<String> variables;

    public ReadStatement(List<String> variables) {
        this.variables = variables;
    }

    public List<String> getVariables() { return variables; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(ind).append("{\n");
        sb.append(ind).append("  \"type\": \"ReadStatement\",\n");
        sb.append(ind).append("  \"variables\": [");
        for (int i = 0; i < variables.size(); i++) {
            sb.append("\"").append(variables.get(i)).append("\"");
            if (i < variables.size() - 1) sb.append(", ");
        }
        sb.append("]\n");
        sb.append(ind).append("}");
        return sb.toString();
    }
}
