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

        stackDistance += 1;

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

        // we add the parameters so they don't show up as the free variables
        // add the parameters to the address environment
        Environment previous = environment;
        Environment functionEnvironment = new Environment();

        int i = 0;
        for(Expr.Variable variable : functionDefinition.variables){
            functionEnvironment.insert(variable.varName, Visibility.L, -i);
            i++;
        }

        Set<Expr.Variable> freeVariables = free(functionDefinition.rightHandSide, functionEnvironment);

        int globVars = 0;
        for(Expr.Variable var : freeVariables){

            functionEnvironment.insert(var.varName, Visibility.G, globVars++);

            // with respect to the calling scope
            code.addCode(getVar(var));
        }

        int g = freeVariables.size();

        code.addInstruction(new Instr.MakeVec(g), stackDistance);

        // make vec consumes all the g arguments (so -g) and adds one address (the one to the global vector)
        // on the stack
        stackDistance = stackDistance - g + 1;

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

        // now, we are in the function body so we need the function environment
        environment = functionEnvironment;

        code.addCode(codeV(functionDefinition.rightHandSide));
        code.addInstruction(new Instr.Return(k), stackDistance);

        // now the code after the function

        // outside function again so old stack distance has to be restored
        stackDistance = previousStackDistance;

        // for future code (we don't know yet what comes here)
        code.setJumpLabelAtEnd(jumpOverFunction);

        // restore old environment
        environment = previous;

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

        if(expr instanceof Expr.Variable){
            if(!surroundingEnvironment.env.containsKey(((Expr.Variable) expr).varName)){
                freeVars.add((Expr.Variable) expr);
            }
        }
        else if(expr instanceof Expr.UnOp){
            return free(((Expr.UnOp)expr).expr, surroundingEnvironment);
        }
        else if(expr instanceof Expr.BinOp){

            Set<Expr.Variable> freeVarsLeft = free(((Expr.BinOp) expr).left, surroundingEnvironment);
            Set<Expr.Variable> freeVarsRight = free(((Expr.BinOp) expr).right, surroundingEnvironment);
            freeVarsLeft.addAll(freeVarsRight);

            return freeVarsLeft;
        }
        else if(expr instanceof Expr.FunctionDefinition){
            // the parameters are now defined, at least they are not free anymore
            Environment newSurrounding = surroundingEnvironment.deepCopy();

            for(Expr.Variable var : ((Expr.FunctionDefinition) expr).variables){
                // the address is not really important for this 'free' check
                newSurrounding.insert(var.varName, Visibility.L, 0);
            }

            return free(((Expr.FunctionDefinition) expr).rightHandSide, newSurrounding);
        }
        else if(expr instanceof Expr.Let){
           Environment newSurrounding = surroundingEnvironment.deepCopy();

           newSurrounding.insert(((Expr.Let) expr).target.varName, Visibility.L, 0);

           return free(((Expr.Let) expr).inExpr, newSurrounding);
        }
        else if(expr instanceof Expr.FunctionApplication){
            Set<Expr.Variable> freeFunction = free(((Expr.FunctionApplication) expr).functionExpr, surroundingEnvironment);

            for(Expr argumentExpr : ((Expr.FunctionApplication) expr).exprArguments){
                freeFunction.addAll(free(argumentExpr, surroundingEnvironment));
            }
        }

        return freeVars;
    }

    @Override
    public Code visitFunctionApplication(Expr.FunctionApplication functionApplication, GenerationMode mode) {

        Code code = new Code();

        // we save the
        String continueAfterFunctionCall = code.getNewJumpLabel();

        code.addInstruction(new Instr.Mark(continueAfterFunctionCall), stackDistance);

        // reserve three spots for the organizational cells
        stackDistance += 3;

        // push all the arguments on the stack, start from right to left (that's why reversed)
        for(Expr exprArgument : functionApplication.exprArguments.reversed()){
            code.addCode(codeV(exprArgument));
        }

        code.addCode(codeV(functionApplication.functionExpr));

        code.addInstruction(new Instr.Apply(), stackDistance);

        code.setJumpLabelAtEnd(continueAfterFunctionCall);

        return code;
    }

    @Override
    public Code visitIf(Expr.If ifExpr, GenerationMode mode) {
        return null;
    }
}
