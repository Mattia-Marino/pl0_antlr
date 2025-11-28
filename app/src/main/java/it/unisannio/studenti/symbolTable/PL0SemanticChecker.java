package it.unisannio.studenti.symbolTable;

import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import grammar.PL0BaseListener;
import grammar.PL0Parser;

/**
 * Phase 2: Performs Semantic Analysis using the built Symbol Table.
 */
public class PL0SemanticChecker extends PL0BaseListener {
    private final SymbolTable symbolTable;
    private int errorCount = 0;

    public PL0SemanticChecker(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    public int getErrorCount() {
        return errorCount;
    }

    private void reportError(String message) {
        System.err.println(message);
        errorCount++;
    }

    @Override
    public void enterProcedureDeclaration(PL0Parser.ProcedureDeclarationContext ctx) {
        // Enter the corresponding scope created in Phase 1
        symbolTable.enterNextChildScope();
    }

    @Override
    public void exitProcedureDeclaration(PL0Parser.ProcedureDeclarationContext ctx) {
        symbolTable.exitScope();
    }

    // --- Statements / uses ---

    @Override
    public void enterAssignmentStatement(PL0Parser.AssignmentStatementContext ctx) {
        TerminalNode id = ctx.IDENTIFIER();
        if (id != null) {
            String name = id.getText();
            Symbol symbol = symbolTable.lookup(name);
            
            if (symbol == null) {
                reportError("Semantic Error: Assignment to undeclared identifier '" + name + "'");
            } else if (symbol.getKind() != SymbolKind.VAR) {
                reportError("Semantic Error: Assignment to " + symbol.getKind() + " '" + name + "' is not allowed.");
            }
        }
    }

    @Override
    public void enterCallStatement(PL0Parser.CallStatementContext ctx) {
        TerminalNode id = ctx.IDENTIFIER();
        if (id != null) {
            String name = id.getText();
            Symbol symbol = symbolTable.lookup(name);
            
            if (symbol == null) {
                reportError("Semantic Error: Call to undeclared procedure '" + name + "'");
            } else if (symbol.getKind() != SymbolKind.PROCEDURE) {
                reportError("Semantic Error: Call to " + symbol.getKind() + " '" + name + "' is not allowed. Expected PROCEDURE.");
            }
        }
    }

    @Override
    public void enterReadStatement(PL0Parser.ReadStatementContext ctx) {
        List<TerminalNode> ids = ctx.IDENTIFIER();
        for (TerminalNode id : ids) {
            String name = id.getText();
            Symbol symbol = symbolTable.lookup(name);
            
            if (symbol == null) {
                reportError("Semantic Error: READ into undeclared identifier '" + name + "'");
            } else if (symbol.getKind() != SymbolKind.VAR) {
                reportError("Semantic Error: READ into " + symbol.getKind() + " '" + name + "' is not allowed. Expected VAR.");
            }
        }
    }

    @Override
    public void enterFactor(PL0Parser.FactorContext ctx) {
        TerminalNode id = ctx.IDENTIFIER();
        if (id != null) {
            String name = id.getText();
            Symbol symbol = symbolTable.lookup(name);
            
            if (symbol == null) {
                reportError("Semantic Error: Use of undeclared identifier '" + name + "'");
            } else if (symbol.getKind() == SymbolKind.PROCEDURE) {
                reportError("Semantic Error: Procedure '" + name + "' cannot be used in an expression.");
            }
        }
    }
}
