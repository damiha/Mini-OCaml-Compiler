import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Compiler implements Expr.Visitor<Code>{

    // important to keep track of everything?
    int stackDistance = 0;

    Environment environment;

    public Compiler(){
        environment = new Environment();
    }

    public Code codeB(Expr expr){
        return expr.accept(this, GenerationMode.B);
    }

    public Code codeV(Expr expr){
        return expr.accept(this, GenerationMode.V);
    }

    @Override
    public Code visitIntLiteral(Expr.IntLiteral intLiteral, GenerationMode mode) {

        Code code = new Code();

        if(mode == GenerationMode.B || mode == GenerationMode.V){
            code.addInstruction(new Instr.LoadC(intLiteral.value), stackDistance);
            stackDistance += 1;
        }

        if(mode == GenerationMode.V){
            // this swaps the value with the address in the heap so now stack increase
            code.addInstruction(new Instr.MakeBasic(), stackDistance);
        }

        return code;
    }

    @Override
    public Code visitVariable(Expr.Variable variable, GenerationMode mode) {

        Code code = new Code();

        code.addCode(getVar(variable));
        stackDistance += 1;

        if(mode == GenerationMode.B){
            code.addInstruction(new Instr.GetBasic(), stackDistance);
        }

        return code;
    }

    private Code getVar(Expr.Variable variable){
        Code code = new Code();

        Pair<Visibility, Integer> pair = environment.get(variable.varName);

        if(pair.first() == Visibility.L){
            code.addInstruction(new Instr.PushLoc(stackDistance - pair.second()), stackDistance);
        }
        else{
            code.addInstruction(new Instr.PushGlob(pair.second()), stackDistance);
        }

        return code;
    }

    @Override
    public Code visitLet(Expr.Let let, GenerationMode mode) {

        Code code = new Code();

        Expr current = let;

        // restore this after the let expression
        Environment previous = environment;

        environment = environment.deepCopy();

        int letCounter = 0;

        do{
            code.addCode(codeV(((Expr.Let) current).rightHandSide));
            letCounter += 1;

            // we insert after the codeV because only when the right hand side exists, the variable is defined
            environment.insert(((Expr.Let) current).target.varName, Visibility.L, stackDistance);

            current = ((Expr.Let) current).inExpr;

        }while(current instanceof Expr.Let);

        // final expression is not a let
        code.addCode(codeV(current));

        // slide n to destroy all variables in the context
        code.addInstruction(new Instr.Slide(letCounter), stackDistance);

        environment = previous;

        return code;
    }

    @Override
    public Code visitUnOp(Expr.UnOp unOp, GenerationMode mode) {
        return null;
    }

    @Override
    public Code visitBinOp(Expr.BinOp binOp, GenerationMode mode) {

        Code code = new Code();

        // binary operation must actually calculate with real values (not just heap addresses)
        // so we use codeB
        code.addCode(codeB(binOp.left));
        code.addCode(codeB(binOp.right));

        switch(binOp.operator){
            case BinaryOperator.PLUS:
                code.addInstruction(new Instr.Add(), stackDistance);
                break;
            case BinaryOperator.MUL:
                code.addInstruction(new Instr.Mul(), stackDistance);
                break;
        }

        // one operator is consumed
        stackDistance -= 1;

        if(mode == GenerationMode.V){
            code.addInstruction(new Instr.MakeBasic(), stackDistance);
        }

        return code;
    }

    @Override
    public Code visitLetRec(Expr.LetRec letRec, GenerationMode mode) {
        return null;
    }

    @Override
    public Code visitFunctionDefinition(Expr.FunctionDefinition functionDefinition, GenerationMode mode) {

        Code code = new Code();

        // first, gather all free variables (not function parameters, not locals)
        // and wrap them in a global vector
        Set<Expr.Variable> freeVariables = free(functionDefinition.rightHandSide);

        for(Expr.Variable var : freeVariables){
            code.addCode(codeV(var));
        }

        int g = freeVariables.size();
        code.addInstruction(new Instr.MakeVec(g), stackDistance);

        String jumpToFunction = code.getNewJumpLabel();
        String jumpOverFunction = code.getNewJumpLabel();

        code.addInstruction(new Instr.MakeFunVal(jumpToFunction), stackDistance);
        code.addInstruction(new Instr.Jump(jumpOverFunction), stackDistance);

        // now the code for the function

        // are exactly all k values the function needs present or is it over or under supplied
        int k = functionDefinition.variables.size();

        int previousStackDistance = stackDistance;
        stackDistance = 0;

        code.addInstruction(new Instr.TestArg(k), jumpToFunction, stackDistance);
        code.addCode(codeV(functionDefinition.rightHandSide));
        code.addInstruction(new Instr.Return(k), stackDistance);

        // now the code after the function

        // outside function again so old stack distance has to be restored
        stackDistance = previousStackDistance;

        // for future code (we don't know yet what comes here)
        code.setJumpLabelAtEnd(jumpOverFunction);

        return code;
    }

    private Set<Expr.Variable> free(Expr expr){

        Environment previous = environment;
        environment = environment.deepCopy();

        Set<Expr.Variable> freeVariables = free(expr, environment);

        // restore old environment
        environment = previous;

        return freeVariables;
    }

    private Set<Expr.Variable> free(Expr expr, Environment surroundingEnvironment){

        Set<Expr.Variable> freeVars = new HashSet<>();

        // TODO: CONTINUE HERE
        if(expr instanceof Expr.Variable){
            if(!surroundingEnvironment.env.containsKey(((Expr.Variable) expr).varName)){
                freeVars.add((Expr.Variable) expr);
            }
        }

        return freeVars;
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
