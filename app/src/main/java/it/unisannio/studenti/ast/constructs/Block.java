package it.unisannio.studenti.ast.constructs;

import java.util.List;
import java.util.ArrayList;

import it.unisannio.studenti.ast.ASTVisitor;

public class Block implements ASTNode {
    private List<ConstDeclaration> consts;
    private List<VarDeclaration> vars;
    private List<ProcedureDeclaration> procs;
    private Statement statement;

    public Block(List<ConstDeclaration> consts, List<VarDeclaration> vars, List<ProcedureDeclaration> procs, Statement statement) {
        this.consts = consts != null ? consts : new ArrayList<>();
        this.vars = vars != null ? vars : new ArrayList<>();
        this.procs = procs != null ? procs : new ArrayList<>();
        this.statement = statement;
    }

    public List<ConstDeclaration> getConsts() { return consts; }
    public List<VarDeclaration> getVars() { return vars; }
    public List<ProcedureDeclaration> getProcs() { return procs; }
    public Statement getStatement() { return statement; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        String ind2 = getIndent(indent + 1);
        StringBuilder sb = new StringBuilder();
        sb.append(ind).append("{\n");
        sb.append(ind).append("  \"type\": \"Block\",\n");
        
        sb.append(ind).append("  \"constants\": [\n");
        for (int i = 0; i < consts.size(); i++) {
            sb.append(consts.get(i).toJson(indent + 2));
            if (i < consts.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(ind).append("  ],\n");

        sb.append(ind).append("  \"variables\": [\n");
        for (int i = 0; i < vars.size(); i++) {
            sb.append(vars.get(i).toJson(indent + 2));
            if (i < vars.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(ind).append("  ],\n");

        sb.append(ind).append("  \"procedures\": [\n");
        for (int i = 0; i < procs.size(); i++) {
            sb.append(procs.get(i).toJson(indent + 2));
            if (i < procs.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(ind).append("  ],\n");

        sb.append(ind).append("  \"statement\": ").append(statement.toJson(indent + 1)).append("\n");
        sb.append(ind).append("}");
        return sb.toString();
    }
}
