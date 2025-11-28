# PL0 - ANTLR

PL0 compiler developed using ANTLR4

### Instructions to run:
1. Build the project: ```./gradlew clean build```
2. Run the project selecting a test file .pl0: ```./gradlew :app:run --args="<file_name>"```

### Output:
The correct execution of the program produces the following outputs:
1. ```<file_name>-cst.json``` containing a JSON representation of the Concrete Syntax Tree (CST) of the input code
2. ```<file_name>-ast.json``` containing a JSON representation of the Abstract Syntax Tree (AST) of the input code
3. ```<file_name>.s``` is the given file translated in x86 Assembly

The assembly file can then be compiled in an executable using the appropriate compiler, e.g. gcc
```gcc <file_name>.s```