public class Runner {

    public Runner(){}

    public String getOutput(String source){

        Lexer lexer = new Lexer(source);

        Parser parser = new Parser(lexer.getTokens());

        Expr parsedExpr = parser.parse();

        Compiler compiler = new Compiler();

        Code compiledCode = compiler.codeV(parsedExpr);

        VirtualMachine virtualMachine = new VirtualMachine();

        virtualMachine.run(compiledCode);

        HeapElement finalElement = virtualMachine.heap.get(virtualMachine.stack[virtualMachine.stackPointer]);

        return finalElement.getOutputRepresentation();
    }
}
