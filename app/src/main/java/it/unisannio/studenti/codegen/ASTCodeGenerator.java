package it.unisannio.studenti.codegen;

import it.unisannio.studenti.ast.*;
import it.unisannio.studenti.ast.constructs.*;
import it.unisannio.studenti.symbolTable.Symbol;
import it.unisannio.studenti.symbolTable.SymbolKind;
import it.unisannio.studenti.symbolTable.SymbolTable;

public class ASTCodeGenerator implements ASTVisitor<Void> {
	private final SymbolTable symbolTable;
	private final StringBuilder asm = new StringBuilder();
	private int labelCounter = 0;

	public ASTCodeGenerator(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	public String getAssembly() {
		return asm.toString();
	}

	private String newLabel() {
		return "L" + (labelCounter++);
	}

	private void emit(String instruction) {
		asm.append("	").append(instruction).append("\n");
	}

	private void emitLabel(String label) {
		asm.append(label).append(":\n");
	}

	private void loadFramePointer(int targetLevel) {
		int currentLevel = symbolTable.getCurrentNestingLevel();
		emit("mov %rbp, %rbx");
		for (int i = 0; i < currentLevel - targetLevel; i++) {
			emit("mov -8(%rbx), %rbx");
		}
	}

	@Override
	public Void visit(Program program) {
		asm.append(".section .data\n");
		asm.append("fmt_in: .string \"%ld\"\n");
		asm.append("fmt_out: .string \"%ld\\n\"\n");
		asm.append(".section .text\n");
		asm.append(".global main\n");
		asm.append("main:\n");

		emit("push %rbp");
		emit("mov %rsp, %rbp");
		emit("push $0"); // Static link for main (level 0)

		program.getBlock().accept(this);

		emit("mov $0, %rax");
		emit("leave");
		emit("ret");
		return null;
	}

	@Override
	public Void visit(Block block) {
		int localsSize = symbolTable.getLocalsSize();
		
		// Align stack to 16 bytes
		// Current stack state: Return Address (8) + Saved RBP (8) + Static Link (8) = 24 bytes (Misaligned by 8)
		// We need to allocate 'localsSize' + 'padding' such that total allocated is a multiple of 16 + 8
		// effectively making the stack 16-byte aligned.
		// (localsSize + padding) must be such that (localsSize + padding + 8) % 16 == 0
		
		int padding = (16 - (localsSize + 8) % 16) % 16;
		int totalStackSize = localsSize + padding;

		if (totalStackSize > 0) {
			emit("sub $" + totalStackSize + ", %rsp");
		}

		String startLabel = newLabel();
		emit("jmp " + startLabel);

		// Visit procedures
		for (ProcedureDeclaration proc : block.getProcs()) {
			proc.accept(this);
		}

		emitLabel(startLabel);
		block.getStatement().accept(this);
		return null;
	}

	@Override
	public Void visit(ConstDeclaration decl) {
		// Constants are handled during symbol table lookup, not code generation
		return null;
	}

	@Override
	public Void visit(VarDeclaration decl) {
		// Variables are allocated in stack frame (localsSize), no code needed here
		return null;
	}

	@Override
	public Void visit(ProcedureDeclaration decl) {
		String name = decl.getName();
		emitLabel("proc_" + name);
		emit("push %rbp");
		emit("mov %rsp, %rbp");
		emit("push %r10"); // Save static link

		symbolTable.enterNextChildScope();
		decl.getBlock().accept(this);
		symbolTable.exitScope();

		emit("leave");
		emit("ret");
		return null;
	}

	@Override
	public Void visit(AssignmentStatement stmt) {
		stmt.getExpression().accept(this); // Result on stack

		String name = stmt.getVariable();
		Symbol symbol = symbolTable.lookup(name);

		emit("pop %rax");
		loadFramePointer(symbol.getNestingLevel());
		emit("mov %rax, " + symbol.getOffset() + "(%rbx)");
		return null;
	}

	@Override
	public Void visit(CallStatement stmt) {
		String name = stmt.getProcedureName();
		Symbol symbol = symbolTable.lookup(name);

		loadFramePointer(symbol.getNestingLevel());
		emit("mov %rbx, %r10"); // Pass static link in r10
		emit("call proc_" + name);
		return null;
	}

	@Override
	public Void visit(BeginStatement stmt) {
		for (Statement s : stmt.getStatements()) {
			s.accept(this);
		}
		return null;
	}

	@Override
	public Void visit(IfStatement stmt) {
		String elseLabel = newLabel();
		String endLabel = newLabel();

		stmt.getCondition().accept(this); // Pushes 0 (false) or 1 (true)
		emit("pop %rax");
		emit("cmp $0, %rax");
		emit("je " + elseLabel);

		stmt.getThenStatement().accept(this);
		emit("jmp " + endLabel);

		emitLabel(elseLabel);
		if (stmt.getElseStatement() != null) {
			stmt.getElseStatement().accept(this);
		}

		emitLabel(endLabel);
		return null;
	}

	@Override
	public Void visit(WhileStatement stmt) {
		String startLabel = newLabel();
		String endLabel = newLabel();

		emitLabel(startLabel);
		stmt.getCondition().accept(this);
		emit("pop %rax");
		emit("cmp $0, %rax");
		emit("je " + endLabel);

		stmt.getDoStatement().accept(this);
		emit("jmp " + startLabel);

		emitLabel(endLabel);
		return null;
	}

	@Override
	public Void visit(ReadStatement stmt) {
		for (String name : stmt.getVariables()) {
			Symbol symbol = symbolTable.lookup(name);
			
			loadFramePointer(symbol.getNestingLevel());
			// Calculate address of variable
			emit("lea " + symbol.getOffset() + "(%rbx), %rsi"); // Address in rsi
			emit("lea fmt_in(%rip), %rdi"); // Format string
			emit("mov $0, %rax"); // Varargs
			emit("call scanf@PLT"); // Use PLT for shared lib calls
		}
		return null;
	}

	@Override
	public Void visit(WriteStatement stmt) {
		for (Expression expr : stmt.getExpressions()) {
			expr.accept(this);
			emit("pop %rsi"); // Value to print
			emit("lea fmt_out(%rip), %rdi");
			emit("mov $0, %rax");
			emit("call printf@PLT");
		}
		return null;
	}

	@Override
	public Void visit(SkipStatement stmt) {
		// Do nothing
		return null;
	}

	@Override
	public Void visit(OddCondition cond) {
		cond.getExpression().accept(this);
		emit("pop %rax");
		emit("and $1, %rax");
		emit("push %rax");
		return null;
	}

	@Override
	public Void visit(RelationalCondition cond) {
		cond.getLeft().accept(this);
		cond.getRight().accept(this);
		emit("pop %rbx"); // Right
		emit("pop %rax"); // Left
		emit("cmp %rbx, %rax");
		
		String trueLabel = newLabel();
		String endLabel = newLabel();
		String jumpInstr = "";
		
		switch (cond.getOperator()) {
			case "=": jumpInstr = "je"; break;
			case "#": jumpInstr = "jne"; break;
			case "<": jumpInstr = "jl"; break;
			case "<=": jumpInstr = "jle"; break;
			case ">": jumpInstr = "jg"; break;
			case ">=": jumpInstr = "jge"; break;
		}
		
		emit(jumpInstr + " " + trueLabel);
		emit("push $0");
		emit("jmp " + endLabel);
		emitLabel(trueLabel);
		emit("push $1");
		emitLabel(endLabel);
		return null;
	}

	@Override
	public Void visit(BinaryExpression expr) {
		expr.getLeft().accept(this);
		expr.getRight().accept(this);
		
		emit("pop %rbx"); // Right operand
		emit("pop %rax"); // Left operand
		
		String op = expr.getOperator();
		if (op.equals("+")) {
			emit("add %rbx, %rax");
		} else if (op.equals("-")) {
			emit("sub %rbx, %rax");
		} else if (op.equals("*")) {
			emit("imul %rbx, %rax");
		} else if (op.equals("/")) {
			emit("cqo"); // Sign extend rax to rdx:rax
			emit("idiv %rbx");
		}
		emit("push %rax");
		return null;
	}

	@Override
	public Void visit(UnaryExpression expr) {
		expr.getOperand().accept(this);
		
		if (expr.getOperator().equals("-")) {
			emit("pop %rax");
			emit("neg %rax");
			emit("push %rax");
		}
		return null;
	}

	@Override
	public Void visit(NumberLiteral expr) {
		emit("mov $" + expr.getValue() + ", %rax");
		emit("push %rax");
		return null;
	}

	@Override
	public Void visit(VariableAccess expr) {
		String name = expr.getName();
		Symbol symbol = symbolTable.lookup(name);
		if (symbol.getKind() == SymbolKind.CONST) {
			emit("mov $" + symbol.getValue() + ", %rax");
			emit("push %rax");
		} else {
			loadFramePointer(symbol.getNestingLevel());
			emit("mov " + symbol.getOffset() + "(%rbx), %rax");
			emit("push %rax");
		}
		return null;
	}
}
