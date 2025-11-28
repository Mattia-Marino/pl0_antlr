package it.unisannio.studenti.symbolTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the symbol table for the PL/0 language, supporting block-structured
 * scoping necessary for procedure declarations.
 */
public class SymbolTable {

    private static class Scope {
        final int id;
        final Scope parent;
        final List<Scope> children = new ArrayList<>();
        final Map<String, Symbol> symbols = new HashMap<>();
        int nextChildIndex = 0; // For traversal during semantic analysis
        int nestingLevel;
        int currentOffset = -16; // Start local variables at rbp-16 (rbp-8 is static link)

        Scope(int id, Scope parent, int nestingLevel) {
            this.id = id;
            this.parent = parent;
            this.nestingLevel = nestingLevel;
        }
    }

    private Scope root;
    private Scope currentScope;
    private int scopeCounter = 0;

    public SymbolTable() {
        // Initialize with Global Scope
        this.root = new Scope(scopeCounter++, null, 0);
        this.currentScope = root;
    }

    // --- Scope Management ---

    /**
     * Enters a new, empty scope (used during Symbol Table Construction).
     */
    public void enterNewScope() {
        Scope newScope = new Scope(scopeCounter++, currentScope, currentScope.nestingLevel + 1);
        currentScope.children.add(newScope);
        currentScope = newScope;
    }

    /**
     * Enters the next child scope (used during Semantic Analysis).
     */
    public void enterNextChildScope() {
        if (currentScope.nextChildIndex < currentScope.children.size()) {
            currentScope = currentScope.children.get(currentScope.nextChildIndex++);
        } else {
            System.err.println("Error: No more child scopes to enter.");
        }
    }

    /**
     * Exits the current scope, making the previous scope the current one.
     */
    public void exitScope() {
        if (currentScope.parent != null) {
            currentScope = currentScope.parent;
        } else {
            System.err.println("Error: Cannot exit the global scope.");
        }
    }

    /**
     * Resets the symbol table traversal state for the second pass.
     */
    public void reset() {
        this.currentScope = root;
        resetIterators(root);
    }

    private void resetIterators(Scope scope) {
        scope.nextChildIndex = 0;
        for (Scope child : scope.children) {
            resetIterators(child);
        }
    }

    public int getLocalsSize() {
        // Initial offset is -16.
        // Size = (-currentScope.currentOffset) - 16
        return (-currentScope.currentOffset) - 16;
    }

    public int getCurrentNestingLevel() {
        return currentScope.nestingLevel;
    }

    // --- Symbol Declaration and Lookup ---

    public boolean addSymbol(String id, SymbolKind kind) {
        return addSymbol(id, kind, null);
    }

    public boolean addSymbol(String id, SymbolKind kind, Integer value) {
        if (currentScope.symbols.containsKey(id)) {
            System.err.println("Semantic Error: Identifier '" + id + "' already declared in the current scope.");
            return false;
        } else {
            Symbol symbol = new Symbol(id, kind, value);
            symbol.setNestingLevel(currentScope.nestingLevel);
            if (kind == SymbolKind.VAR) {
                symbol.setOffset(currentScope.currentOffset);
                currentScope.currentOffset -= 8;
            }
            currentScope.symbols.put(id, symbol);
            return true;
        }
    }

    public Symbol lookup(String id) {
        Scope scope = currentScope;
        while (scope != null) {
            if (scope.symbols.containsKey(id)) {
                return scope.symbols.get(id);
            }
            scope = scope.parent;
        }
        return null;
    }

    public boolean contains(String id) {
        return lookup(id) != null;
    }

    // --- Utility and Debugging ---

    public void printTable() {
        System.out.println("\n--- Symbol Table (PL/0) ---");
        printScope(root);
        System.out.println("---------------------------\n");
    }

    private void printScope(Scope scope) {
        String scopeName = (scope.parent == null) ? "GLOBAL (Scope " + scope.id + ")" : "Procedure/Block (Scope " + scope.id + ")";
        System.out.println("\n" + scopeName + ":");
        
        if (scope.symbols.isEmpty()) {
            System.out.println("  (Empty)");
        } else {
            scope.symbols.forEach((key, symbol) -> 
                System.out.printf("  %-10s : %s%n", key, symbol)
            );
        }

        for (Scope child : scope.children) {
            printScope(child);
        }
    }
}
