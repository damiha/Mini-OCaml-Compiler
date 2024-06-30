public class ValueRetriever {

    Lexer lexer;
    Parser parser;
    Compiler compiler;
    VirtualMachine virtualMachine;

    Expr parsedExpr;
    Code compiledCode;

    public ValueRetriever(String source){

        lexer = new Lexer(source);

        parser = new Parser(lexer.getTokens());

        parsedExpr = parser.parse();

        compiler = new Compiler();

        compiledCode = compiler.codeB(parsedExpr);

        virtualMachine = new VirtualMachine();
    }

    public int retrieveValue(){
        return virtualMachine.run(compiledCode);
    }


}
