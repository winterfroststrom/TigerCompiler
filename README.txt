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

Because the IR does not have convenient comparision operators, branches were used.
This means the temporary registers are live between basic blocks, 
which means the temporaries are converted to variables.

All IR variables and labels and registers are renamed to an ascii number format to account 
for the limited alphabet accepted for MIPS labels.

A IR opcode META_EXACT has been added to allow the implementation of system commands 
because there is no convenient IR equivalent.
Similarly, code generation also renames registers to MIPS registers 
because register-memory operations are hard to represent in the IR.

The class BasicBlock stores is a basic block.
The control flow graph is represented as a list of BasicBlocks.
If the Configuration.java file is modified to enable MIPS_COMMENTS, 
then each segment of mips code will be annotated with the corresponding basic block number (numbering is based on instruction position)
as well as an indicator of the successor and predecessor blocks by their block numbers.
The BasicBlock class has a label instruction, a jump instruction, a list of instructions, and variables.
While adjacent blocks could use goto's to direct themselves to the next block to enable arbitrary ordering,
the original order is maintained for simplicity.

Graph coloring is implemented by using liveliness analysis and forward propagating the du-chains based on 
the calculated liveliness information. 
Since EBB blocks have only one entry points, this means a simple BFS is enough to extend du-chain analysis for EBBs.

Functions are implemented by pushing the parameter values to the stack before a call and restoring them before the return statement.
The parameters to a function are placed in the global function parameter labels, which allows them to be treated as normal variables.
This also obviates the need for frame pointers because the stack is only ever used for spilling-like purposes.

Because it was easier to implement, there are actually two code generators.
They both work via ad hoc one-to-one conversion from IR to mips.
They are the NaiveCodeGenerator and the RegisterCodeGenerator.
The first generates code while always spilling variables, and the second uses a register map to avoid spilling.

For a nice game of tic tac toe, please compile e7.tiger.

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
