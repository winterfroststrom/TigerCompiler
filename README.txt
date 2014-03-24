---------------------------------------------------------------------------------
ORGANIZATION
---------------------------------------------------------------------------------

The Scanner and Parser codes are located in workspace/TigerCompiler/src,
respectively inside the folders Lexer and Parser.

The hand-written tables for the DFA, Tiger grammar and parser can be found in
the PDF report submitted with the project.

The Parser also generates a parse tree represented by the ParseTreeNode class.
The parse tree adds left associativity by collapsing expr and stat-assign 
sub-trees and usings an implementation of the shunting-yard algorithm to 
generate a basic abstract syntax tree.

The Parser gives the resulting tree to the Semantic Checker, which traverses 
the tree to build a symbol table.
Because it was simplier, the process of building the symbol table actually
does minor amounts of type checking.
The symbol table stores three hashmaps : 
* one for types
* one for the L1 namespace of functions and variables
* one for the parameter types of functions

The Semantic Checker traverses the tree with the symbol table and type checks 
the rest of the tree.

---------------------------------------------------------------------------------
RUNNING
---------------------------------------------------------------------------------

A JAR file is provided to make it easier to run the project, so by typing:

java -jar TigerCompiler.jar <tiger_file_path>

Once you run the project, the .tokens and .tokens.err files will be automatically
generated in the same path of the Tiger file.


---------------------------------------------------------------------------------
COMPILING
---------------------------------------------------------------------------------

In case you want to compile the project by yourself, you only have to create a
Java project and pass the workspace path, naming the project as TigerCompiler.


---------------------------------------------------------------------------------
STUDENTS
---------------------------------------------------------------------------------
David Zhang
Matheus Smythe Svolenski
