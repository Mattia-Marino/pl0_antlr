package it.unisannio.studenti.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTree;

import grammar.PL0BaseVisitor;
import grammar.PL0Parser;

import it.unisannio.studenti.ast.constructs.*;

public class PL0ASTBuilder extends PL0BaseVisitor<Object> {

    @Override
    public Object visitProgram(PL0Parser.ProgramContext ctx) {
        return new Program((Block) visit(ctx.block()));
    }

    @Override
    public Object visitBlock(PL0Parser.BlockContext ctx) {
        List<ConstDeclaration> consts = new ArrayList<>();
        if (ctx.constDeclaration() != null) {
            consts.addAll((List<ConstDeclaration>) visit(ctx.constDeclaration()));
        }

        List<VarDeclaration> vars = new ArrayList<>();
        if (ctx.varDeclaration() != null) {
            vars.addAll((List<VarDeclaration>) visit(ctx.varDeclaration()));
        }

        List<ProcedureDeclaration> procs = new ArrayList<>();
        for (PL0Parser.ProcedureDeclarationContext p : ctx.procedureDeclaration()) {
            procs.add((ProcedureDeclaration) visit(p));
        }

        Statement stmt = (Statement) visit(ctx.statement());

        return new Block(consts, vars, procs, stmt);
    }

    @Override
    public Object visitConstDeclaration(PL0Parser.ConstDeclarationContext ctx) {
        List<ConstDeclaration> list = new ArrayList<>();
        for (int i = 0; i < ctx.IDENTIFIER().size(); i++) {
            String name = ctx.IDENTIFIER(i).getText();
            int value = Integer.parseInt(ctx.NUMBER(i).getText());
            list.add(new ConstDeclaration(name, value));
        }
        return list;
    }

    @Override
    public Object visitVarDeclaration(PL0Parser.VarDeclarationContext ctx) {
        List<VarDeclaration> list = new ArrayList<>();
        for (TerminalNode id : ctx.IDENTIFIER()) {
            list.add(new VarDeclaration(id.getText()));
        }
        return list;
    }

    @Override
    public Object visitProcedureDeclaration(PL0Parser.ProcedureDeclarationContext ctx) {
        String name = ctx.IDENTIFIER().getText();
        Block block = (Block) visit(ctx.block());
        return new ProcedureDeclaration(name, block);
    }

    @Override
    public Object visitStatement(PL0Parser.StatementContext ctx) {
        if (ctx.assignmentStatement() != null) return visit(ctx.assignmentStatement());
        if (ctx.callStatement() != null) return visit(ctx.callStatement());
        if (ctx.beginStatement() != null) return visit(ctx.beginStatement());
        if (ctx.ifStatement() != null) return visit(ctx.ifStatement());
        if (ctx.whileStatement() != null) return visit(ctx.whileStatement());
        if (ctx.readStatement() != null) return visit(ctx.readStatement());
        if (ctx.writeStatement() != null) return visit(ctx.writeStatement());
        if (ctx.skipStatement() != null) return visit(ctx.skipStatement());
        return null;
    }

    @Override
    public Object visitAssignmentStatement(PL0Parser.AssignmentStatementContext ctx) {
        String var = ctx.IDENTIFIER().getText();
        Expression expr = (Expression) visit(ctx.expression());
        return new AssignmentStatement(var, expr);
    }

    @Override
    public Object visitCallStatement(PL0Parser.CallStatementContext ctx) {
        return new CallStatement(ctx.IDENTIFIER().getText());
    }

    @Override
    public Object visitBeginStatement(PL0Parser.BeginStatementContext ctx) {
        List<Statement> stmts = new ArrayList<>();
        for (PL0Parser.StatementContext s : ctx.statement()) {
            stmts.add((Statement) visit(s));
        }
        return new BeginStatement(stmts);
    }

    @Override
    public Object visitIfStatement(PL0Parser.IfStatementContext ctx) {
        Condition cond = (Condition) visit(ctx.condition());
        Statement thenStmt = (Statement) visit(ctx.statement(0));
        Statement elseStmt = null;
        if (ctx.statement().size() > 1) {
            elseStmt = (Statement) visit(ctx.statement(1));
        }
        return new IfStatement(cond, thenStmt, elseStmt);
    }

    @Override
    public Object visitWhileStatement(PL0Parser.WhileStatementContext ctx) {
        Condition cond = (Condition) visit(ctx.condition());
        Statement doStmt = (Statement) visit(ctx.statement());
        return new WhileStatement(cond, doStmt);
    }

    @Override
    public Object visitReadStatement(PL0Parser.ReadStatementContext ctx) {
        List<String> vars = new ArrayList<>();
        for (TerminalNode id : ctx.IDENTIFIER()) {
            vars.add(id.getText());
        }
        return new ReadStatement(vars);
    }

    @Override
    public Object visitWriteStatement(PL0Parser.WriteStatementContext ctx) {
        List<Expression> exprs = new ArrayList<>();
        for (PL0Parser.ExpressionContext e : ctx.expression()) {
            exprs.add((Expression) visit(e));
        }
        return new WriteStatement(exprs);
    }

    @Override
    public Object visitSkipStatement(PL0Parser.SkipStatementContext ctx) {
        return new SkipStatement();
    }

    @Override
    public Object visitCondition(PL0Parser.ConditionContext ctx) {
        if (ctx.K_ODD() != null) {
            return new OddCondition((Expression) visit(ctx.expression(0)));
        } else {
            Expression left = (Expression) visit(ctx.expression(0));
            String op = ctx.relation().getText();
            Expression right = (Expression) visit(ctx.expression(1));
            return new RelationalCondition(left, op, right);
        }
    }

    @Override
    public Object visitExpression(PL0Parser.ExpressionContext ctx) {
        int childIndex = 0;
        String firstOp = null;
        
        // Check for unary operator
        if (ctx.getChild(0) instanceof TerminalNode && 
           (ctx.getChild(0).getText().equals("+") || ctx.getChild(0).getText().equals("-"))) {
            firstOp = ctx.getChild(0).getText();
            childIndex++;
        }
        
        Expression currentExpr = (Expression) visit(ctx.term(0));
        if (firstOp != null) {
            currentExpr = new UnaryExpression(firstOp, currentExpr);
        }
        
        // We have consumed term(0) and optional unary op.
        // Now we look for (op term)*
        // The children list is: [unaryOp?] term0 op1 term1 op2 term2 ...
        
        // Let's iterate through the remaining children
        // We need to find where term(0) ends in the child list.
        // It's easier to just iterate terms and find the operator before them.
        
        for (int i = 1; i < ctx.term().size(); i++) {
            // Find the operator before term(i)
            // term(i) is a child.
            // The operator is the child immediately before term(i).
            
            ParseTree termNode = ctx.term(i);
            // Find index of termNode in children
            int termChildIndex = -1;
            for(int k=0; k<ctx.getChildCount(); k++) {
                if(ctx.getChild(k) == termNode) {
                    termChildIndex = k;
                    break;
                }
            }
            
            String op = ctx.getChild(termChildIndex - 1).getText();
            Expression right = (Expression) visit(termNode);
            currentExpr = new BinaryExpression(currentExpr, op, right);
        }
        
        return currentExpr;
    }

    @Override
    public Object visitTerm(PL0Parser.TermContext ctx) {
        Expression currentExpr = (Expression) visit(ctx.factor(0));
        
        for (int i = 1; i < ctx.factor().size(); i++) {
            ParseTree factorNode = ctx.factor(i);
            int factorChildIndex = -1;
            for(int k=0; k<ctx.getChildCount(); k++) {
                if(ctx.getChild(k) == factorNode) {
                    factorChildIndex = k;
                    break;
                }
            }
            
            String op = ctx.getChild(factorChildIndex - 1).getText();
            Expression right = (Expression) visit(factorNode);
            currentExpr = new BinaryExpression(currentExpr, op, right);
        }
        return currentExpr;
    }

    @Override
    public Object visitFactor(PL0Parser.FactorContext ctx) {
        if (ctx.IDENTIFIER() != null) {
            return new VariableAccess(ctx.IDENTIFIER().getText());
        } else if (ctx.NUMBER() != null) {
            return new NumberLiteral(Integer.parseInt(ctx.NUMBER().getText()));
        } else {
            return visit(ctx.expression());
        }
    }
}
