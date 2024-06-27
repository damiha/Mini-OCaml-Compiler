public class Compiler implements Expr.Visitor<Code>{

    // important to keep track of everything?
    int stackDistance = 0;

    public Compiler(){

    }

    public Code codeB(Expr expr){
        return expr.accept(this, GenerationMode.B);
    }

    public Code codeV(Expr expr){
        return expr.accept(this, GenerationMode.V);
    }

    public Code codeC(Expr expr){
        return expr.accept(this, GenerationMode.C);
    }

    @Override
    public Code visitIntLiteral(Expr.IntLiteral intLiteral, GenerationMode mode) {

        Code code = new Code();

        if(mode == GenerationMode.B || mode == GenerationMode.V){
            code.addInstruction(new Instr.LoadC(intLiteral.value));
            stackDistance += 1;
        }

        if(mode == GenerationMode.V){
            // this swaps the value with the address in the heap so now stack increase
            code.addInstruction(new Instr.GetBasic());
        }

        return code;
    }

    @Override
    public Code visitUnOp(Expr.UnOp unOp, GenerationMode mode) {
        return null;
    }

    @Override
    public Code visitBinOp(Expr.BinOp binOp, GenerationMode mode) {
        return null;
    }

    @Override
    public Code visitVariable(Expr.Variable variable, GenerationMode mode) {
        return null;
    }

    @Override
    public Code visitLet(Expr.Let let, GenerationMode mode) {
        return null;
    }

    @Override
    public Code visitLetRec(Expr.LetRec letRec, GenerationMode mode) {
        return null;
    }

    @Override
    public Code visitFunctionDefinition(Expr.FunctionDefinition functionDefinition, GenerationMode mode) {
        return null;
    }

    @Override
    public Code visitFunctionApplication(Expr.FunctionApplication functionApplication, GenerationMode mode) {
        return null;
    }

    @Override
    public Code visitIf(Expr.If ifExpr, GenerationMode mode) {
        return null;
    }
}
