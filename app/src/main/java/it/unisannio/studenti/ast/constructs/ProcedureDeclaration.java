package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public class ProcedureDeclaration implements ASTNode {
    private String name;
    private Block block;

    public ProcedureDeclaration(String name, Block block) {
        this.name = name;
        this.block = block;
    }

    public String getName() { return name; }
    public Block getBlock() { return block; }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toJson(int indent) {
        String ind = getIndent(indent);
        StringBuilder sb = new StringBuilder();
        sb.append(ind).append("{\n");
        sb.append(ind).append("  \"type\": \"ProcedureDeclaration\",\n");
        sb.append(ind).append("  \"name\": \"").append(name).append("\",\n");
        sb.append(ind).append("  \"block\": ").append(block.toJson(indent + 1)).append("\n");
        sb.append(ind).append("}");
        return sb.toString();
    }
}
