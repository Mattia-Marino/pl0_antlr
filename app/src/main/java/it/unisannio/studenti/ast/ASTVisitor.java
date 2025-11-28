package it.unisannio.studenti.ast;

import it.unisannio.studenti.ast.constructs.*;

public interface ASTVisitor<T> {
    T visit(Program program);
    T visit(Block block);
    T visit(ConstDeclaration decl);
    T visit(VarDeclaration decl);
    T visit(ProcedureDeclaration decl);
    
    T visit(AssignmentStatement stmt);
    T visit(CallStatement stmt);
    T visit(BeginStatement stmt);
    T visit(IfStatement stmt);
    T visit(WhileStatement stmt);
    T visit(ReadStatement stmt);
    T visit(WriteStatement stmt);
    T visit(SkipStatement stmt);
    
    T visit(BinaryExpression expr);
    T visit(UnaryExpression expr);
    T visit(NumberLiteral expr);
    T visit(VariableAccess expr);
    
    T visit(OddCondition cond);
    T visit(RelationalCondition cond);
}
