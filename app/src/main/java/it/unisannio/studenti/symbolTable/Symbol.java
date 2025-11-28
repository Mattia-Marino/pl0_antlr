package it.unisannio.studenti.symbolTable;

public class Symbol {
    private String name;
    private SymbolKind kind;
    // For constants, we might want to store the value.
    // For procedures, we might want to store parameter count (though PL/0 usually has 0).
    // For now, we'll keep it simple but extensible.
    private Integer value; // Null if not a constant or not known
    private int nestingLevel;
    private int offset; // For variables: offset from FP. For procedures: maybe not needed or entry address?

    public Symbol(String name, SymbolKind kind) {
        this.name = name;
        this.kind = kind;
    }

    public Symbol(String name, SymbolKind kind, Integer value) {
        this.name = name;
        this.kind = kind;
        this.value = value;
    }

    public void setNestingLevel(int nestingLevel) {
        this.nestingLevel = nestingLevel;
    }

    public int getNestingLevel() {
        return nestingLevel;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public String getName() {
        return name;
    }

    public SymbolKind getKind() {
        return kind;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value != null) {
            return String.format("%s (%s, value=%d)", name, kind, value);
        }
        return String.format("%s (%s)", name, kind);
    }
}
