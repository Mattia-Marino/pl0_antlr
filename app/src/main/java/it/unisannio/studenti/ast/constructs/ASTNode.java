package it.unisannio.studenti.ast.constructs;

import it.unisannio.studenti.ast.ASTVisitor;

public interface ASTNode {
    String toJson(int indent);
    <T> T accept(ASTVisitor<T> visitor);
    
    default String getIndent(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) sb.append("  ");
        return sb.toString();
    }
}
