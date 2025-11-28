package it.unisannio.studenti.symbolTable;

import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

import grammar.PL0BaseListener;
import grammar.PL0Parser;

/**
 * Phase 1: Builds the Symbol Table structure (scopes and declarations).
 */
public class PL0SymbolTableBuilder extends PL0BaseListener {
    private final SymbolTable symbolTable;

    public PL0SymbolTableBuilder(SymbolTable symbolTable) {
        this.symbolTable = symbolTable;
    }

    @Override
    public void enterConstDeclaration(PL0Parser.ConstDeclarationContext ctx) {
        List<TerminalNode> ids = ctx.IDENTIFIER();
        List<TerminalNode> nums = ctx.NUMBER();
        
        for (int i = 0; i < ids.size(); i++) {
            String name = ids.get(i).getText();
            String numStr = nums.get(i).getText();
            int value = Integer.parseInt(numStr); // TODO: verify if it is just an integer
            symbolTable.addSymbol(name, SymbolKind.CONST, value);
        }
    }

    @Override
    public void enterVarDeclaration(PL0Parser.VarDeclarationContext ctx) {
        List<TerminalNode> ids = ctx.IDENTIFIER();
        for (TerminalNode id : ids) {
            String name = id.getText();
            symbolTable.addSymbol(name, SymbolKind.VAR);
        }
    }

    @Override
    public void enterProcedureDeclaration(PL0Parser.ProcedureDeclarationContext ctx) {
        TerminalNode id = ctx.IDENTIFIER();
        if (id != null) {
            String name = id.getText();
            symbolTable.addSymbol(name, SymbolKind.PROCEDURE);
        }
        // Create new scope for procedure body
        symbolTable.enterNewScope();
    }

    @Override
    public void exitProcedureDeclaration(PL0Parser.ProcedureDeclarationContext ctx) {
        // Exit the procedure scope
        symbolTable.exitScope();
    }
}
