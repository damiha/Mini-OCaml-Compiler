public class Runner {

    Lexer lexer;
    Parser parser;
    Compiler compiler;
    VirtualMachine virtualMachine;

    Expr parsedExpr;
    Code compiledCode;

    public Runner(String source){

        lexer = new Lexer(source);

        parser = new Parser(lexer.getTokens());

        parsedExpr = parser.parse();

        compiler = new Compiler();

        compiledCode = compiler.codeB(parsedExpr);

        virtualMachine = new VirtualMachine();
    }

    public int run(){
        return virtualMachine.run(compiledCode);
    }
}
