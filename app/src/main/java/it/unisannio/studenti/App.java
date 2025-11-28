package it.unisannio.studenti;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.Trees;

import grammar.PL0Lexer;
import grammar.PL0Parser;

import it.unisannio.studenti.ast.PL0ASTBuilder;
import it.unisannio.studenti.ast.constructs.Program;
import it.unisannio.studenti.codegen.ASTCodeGenerator;
import it.unisannio.studenti.errors.SyntaxErrorListener;
import it.unisannio.studenti.symbolTable.PL0SemanticChecker;
import it.unisannio.studenti.symbolTable.PL0SymbolTableBuilder;
import it.unisannio.studenti.symbolTable.SymbolTable;

/**
 * Main application class for the PL/0 Compiler front-end.
 * This class handles file reading, ANTLR setup (Lexer and Parser),
 * and initiates the parsing process for a PL/0 source file.
 */
public class App {

	/**
	 * Entry point of the application.
	 * Expects one command-line argument: the path to the PL/0 source file.
	 * * @param args Command-line arguments. args[0] should be the file path.
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java App <path_to_pl0_file>");
			return;
		}

		String filePath = args[0];
		Path pl0FilePath = Paths.get(filePath);

		System.out.println("--- PL/0 Compiler to x86 Assembly ---");
		System.out.println("Target file: " + pl0FilePath.toAbsolutePath());
		System.out.println("-------------------------------");

		try {
			CharStream input = CharStreams.fromFileName(pl0FilePath.toString());

			// --- Phase 1: Lexical Analysis ---
			System.out.println("\n[PHASE 1] Lexical Analysis...");
			PL0Lexer lexer = new PL0Lexer(input); // Here generating tokens
			SyntaxErrorListener lexerErrorListener = new SyntaxErrorListener();
			lexer.addErrorListener(lexerErrorListener); // Add our listener to count errors
			
			CommonTokenStream tokens = new CommonTokenStream(lexer); // Generate a token stream with lexer
			tokens.fill(); // Force processing of all tokens to detect lexical errors

			if (lexerErrorListener.getErrorCount() > 0) {
				System.err.println("[FAILURE] Lexical Analysis failed with " + lexerErrorListener.getErrorCount() + " error(s).");
				return;
			}
			System.out.println("[SUCCESS] Lexical Analysis complete.");

			// --- Phase 2: Syntax Analysis ---
			System.out.println("\n[PHASE 2] Syntax Analysis...");
			PL0Parser parser = new PL0Parser(tokens);
			
			ParseTree tree = parser.program(); // AKA the derivation three
			
			if (parser.getNumberOfSyntaxErrors() > 0) {
				System.err.println("[FAILURE] Syntax Analysis failed with " + parser.getNumberOfSyntaxErrors() + " error(s).");
				return;
			}
			System.out.println("[SUCCESS] Syntax Analysis complete.");

			System.out.println("\n--- Parse Tree (CST) ---");
			System.out.println(Trees.toStringTree(tree, parser));

			// Generate and write JSON CST
			String jsonCST = toJson(tree, parser);
			String jsonOutputFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + "-cst.json";
			try {
				Files.write(Paths.get(jsonOutputFilePath), jsonCST.getBytes());
				System.out.println("\nParse Tree (CST) JSON written to: " + jsonOutputFilePath);
			} catch (IOException e) {
				System.err.println("\nFailed to write CST JSON: " + e.getMessage());
			}

			// --- Phase 3: Symbol Table Construction ---
			System.out.println("\n[PHASE 3] Symbol Table Construction...");
			SymbolTable symbolTable = new SymbolTable();
			PL0SymbolTableBuilder builder = new PL0SymbolTableBuilder(symbolTable);
			ParseTreeWalker walker = new ParseTreeWalker();
			
			try {
				walker.walk(builder, tree);
				System.out.println("[SUCCESS] Symbol Table constructed.");

				// Print the final Symbol Table
				symbolTable.printTable();
			} catch (Exception e) {
				System.err.println("[FAILURE] Symbol Table construction failed: " + e.getMessage());
				e.printStackTrace();
				return;
			}

			// --- Phase 4: Semantic Analysis ---
			System.out.println("\n[PHASE 4] Semantic Analysis...");
			symbolTable.reset(); // Reset traversal state for the second pass
			PL0SemanticChecker checker = new PL0SemanticChecker(symbolTable);

			try {
				walker.walk(checker, tree);
				if (checker.getErrorCount() > 0) {
					System.err.println("[FAILURE] Semantic Analysis failed with " + checker.getErrorCount() + " error(s).");
					return;
				}
				System.out.println("[SUCCESS] Semantic Analysis complete.");
			} catch (Exception e) {
				System.err.println("[FAILURE] Semantic Analysis failed: " + e.getMessage());
				e.printStackTrace();
				return;
			}

			// --- Phase 4.5: AST Generation ---
			System.out.println("\n[PHASE 4.5] AST Generation...");
			PL0ASTBuilder astBuilder = new PL0ASTBuilder();
			Program ast = (Program) astBuilder.visit(tree);
			
			String astJson = ast.toJson(0);
			System.out.println("--- Abstract Syntax Tree (AST) ---");
			System.out.println(astJson);
			
			String astOutputFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + "-ast.json";
			try {
				Files.write(Paths.get(astOutputFilePath), astJson.getBytes());
				System.out.println("\nAST JSON written to: " + astOutputFilePath);
			} catch (IOException e) {
				System.err.println("\nFailed to write AST JSON: " + e.getMessage());
			}

			// --- Phase 5: Code Generation ---
			System.out.println("\n[PHASE 5] Code Generation...");
			symbolTable.reset(); // Reset traversal state for the third pass
			ASTCodeGenerator generator = new ASTCodeGenerator(symbolTable);

			try {
				generator.visit(ast);
				String assemblyCode = generator.getAssembly();
				
				// Write to file
				String outputFilePath = filePath.substring(0, filePath.lastIndexOf('.')) + ".s";
				Files.write(Paths.get(outputFilePath), assemblyCode.getBytes());

				System.out.println("[SUCCESS] Code Generation complete. Output: " + outputFilePath);
			} catch (Exception e) {
				System.err.println("[FAILURE] Code Generation failed: " + e.getMessage());
				e.printStackTrace();
				return;
			}

			// Print tree with GUI
			// System.out.println("Opening ANTLR Parse Tree Inspector window...");
			// org.antlr.v4.gui.Trees.inspect(tree, parser);

		} catch (IOException e) {
			System.err.println("\n[FATAL ERROR] Could not read the file: " + filePath);
			System.err.println("Details: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("\n[FATAL ERROR] An unexpected error occurred.");
			System.err.println("Details: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static String toJson(ParseTree tree, PL0Parser parser) {
		return toJson(tree, parser, 0);
	}

	private static String toJson(ParseTree tree, PL0Parser parser, int indent) {
		StringBuilder sb = new StringBuilder();
		String indentation = "";
		for (int i = 0; i < indent; i++) indentation += "  ";

		if (tree instanceof org.antlr.v4.runtime.tree.TerminalNode) {
			org.antlr.v4.runtime.tree.TerminalNode node = (org.antlr.v4.runtime.tree.TerminalNode) tree;
			String text = node.getText();
			// Escape special characters for JSON
			text = text.replace("\\", "\\\\")
					   .replace("\"", "\\\"")
					   .replace("\n", "\\n")
					   .replace("\r", "\\r")
					   .replace("\t", "\\t");
			
			sb.append(indentation).append("{\n");
			sb.append(indentation).append("  \"type\": \"terminal\",\n");
			sb.append(indentation).append("  \"text\": \"").append(text).append("\"\n");
			sb.append(indentation).append("}");
		} else if (tree instanceof org.antlr.v4.runtime.RuleContext) {
			org.antlr.v4.runtime.RuleContext ctx = (org.antlr.v4.runtime.RuleContext) tree;
			String ruleName = parser.getRuleNames()[ctx.getRuleIndex()];
			
			sb.append(indentation).append("{\n");
			sb.append(indentation).append("  \"type\": \"rule\",\n");
			sb.append(indentation).append("  \"name\": \"").append(ruleName).append("\",\n");
			sb.append(indentation).append("  \"children\": [\n");
			
			for (int i = 0; i < tree.getChildCount(); i++) {
				sb.append(toJson(tree.getChild(i), parser, indent + 2));
				if (i < tree.getChildCount() - 1) {
					sb.append(",\n");
				} else {
					sb.append("\n");
				}
			}
			
			sb.append(indentation).append("  ]\n");
			sb.append(indentation).append("}");
		}
		return sb.toString();
	}
}