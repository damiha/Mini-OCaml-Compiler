import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Compiler implements Expr.Visitor<Code>{

    // important to keep track of everything?
    int stackDistance = 0;

    Environment environment;

    boolean addDebugInfo = true;

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

        addFallBackForCodeV(code, mode);

        return code;
    }

    @Override
    public Code visitVariable(Expr.Variable variable, GenerationMode mode) {

        Code code = new Code();

        code.addCode(getVar(variable));

        addFallBackForCodeB(code, mode);

        return code;
    }

    @Override
    public Code visitTuple(Expr.Tuple tuple, GenerationMode mode) {

        Code code = new Code();

        // even if we have nested tuples, the stack only increases by one per tuple element
        // because a tuple element is just a pointer to the heap?
        int k = tuple.expressions.size();

        for(Expr expr : tuple.expressions){
            code.addCode(codeV(expr));
        }

        addMakeVec(code, k);

        return code;
    }

    @Override
    public Code visitTupleAccess(Expr.TupleAccess tupleAccess, GenerationMode mode) {

        Code code = codeV(tupleAccess.expr);

        code.addInstruction(new Instr.Get(tupleAccess.j), stackDistance);

        return code;
    }

    @Override
    public Code visitNilExpr(Expr.Nil nil, GenerationMode mode) {
        Code code = new Code();
        code.addInstruction(new Instr.Nil(), stackDistance);
        stackDistance += 1;
        return code;
    }

    @Override
    public Code visitConsExpr(Expr.Cons cons, GenerationMode mode) {
        Code code = new Code();
        code.addCode(codeV(cons.head));
        code.addCode(codeV(cons.tail));
        code.addInstruction(new Instr.Cons(), stackDistance);

        // one operator is consumed and where other is, is now address
        stackDistance -= 1;

        return code;
    }

    @Override
    public Code visitMatchExpr(Expr.Match match, GenerationMode mode) {
        Code code = new Code();

        code.addCode(codeV(match.matchThis));

        String jumpOverNilMatch = code.getNewJumpLabel();
        String jumpOverConsMatch = code.getNewJumpLabel();

        code.addInstruction(new Instr.TList(jumpOverNilMatch), stackDistance);
        stackDistance--;

        int stackDistanceBeforeBranching = stackDistance;

        code.addCode(codeV(match.matchWithNil));

        code.addInstruction(new Instr.Jump(jumpOverConsMatch), stackDistance);

        stackDistance = stackDistanceBeforeBranching;

        code.setJumpLabelAtEnd(jumpOverNilMatch);

        // we need to add the special variables h and t so the function can refer to them
        Environment previous = environment;
        environment = environment.deepCopy();

        environment.insert(
                List.of(new Expr.Variable("t"), new Expr.Variable("h")),
                Visibility.L, stackDistance + 1, Index.INCREASING);

        stackDistance += 2;

        code.addCode(codeV(match.matchWithCons));

        code.addInstruction(new Instr.Slide(2), stackDistance);
        stackDistance -= 2;

        environment = previous;

        code.setJumpLabelAtEnd(jumpOverConsMatch);

        return code;
    }

    private void addMakeVec(Code code, int k){
        code.addInstruction(new Instr.MakeVec(k), stackDistance);

        // make vec consumes all the k arguments (so -k) and adds one address (the one to the global vector) + 1
        // on the stack
        stackDistance = stackDistance - k + 1;
    }

    private Code getVar(Expr.Variable variable){
        Code code = new Code();

        Pair<Visibility, Integer> pair = environment.get(variable.varName);

        if(pair.first() == Visibility.L){
            code.addInstruction(
                    new Instr.PushLoc(
                            stackDistance - pair.second(),
                            addDebugInfo ? environment.deepCopy() : null,
                            addDebugInfo ? stackDistance : -1
                    ),
                    stackDistance);
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

        int slideCounter = 0;

        do{
            code.addCode(codeV(((Expr.Let) current).rightHandSide));

            // we insert after the codeV because only when the right hand side exists, the variable is defined
            int k = environment.insert(((Expr.Let) current).target, Visibility.L, stackDistance, Index.INCREASING);

            // if the target is a tuple, that means the right hand side must also be a tuple
            // so currently, a pointer to a tuple lies on the test
            boolean isTuple = ((Expr.Let) current).target instanceof Expr.Tuple;

            if(isTuple) {
                code.addInstruction(new Instr.GetVec(k), stackDistance);

                // Get(k) puts k on the stack and consumes 1 so k - 1
                stackDistance += (k-1);
            }

            slideCounter += k;

            current = ((Expr.Let) current).inExpr;

        }while(current instanceof Expr.Let);

        // final expression is not a let
        code.addCode(codeV(current));

        // slide n to destroy all variables in the context
        addSlide(code, slideCounter);

        environment = previous;

        addFallBackForCodeB(code, mode);

        return code;
    }

    private void addSlide(Code code, int n){
        code.addInstruction(new Instr.Slide(n), stackDistance);
        stackDistance -= n;
    }

    @Override
    public Code visitLetRec(Expr.LetRec letRec, GenerationMode mode) {

        Code code = new Code();

        int n = letRec.parallelDefs.size();

        environment.insertParallelDefs(letRec.parallelDefs, Visibility.L, stackDistance + 1, Index.INCREASING);

        code.addInstruction(new Instr.Alloc(n), stackDistance);
        stackDistance += n;

        Environment previous = environment;
        environment = environment.deepCopy();

        // since everything is defined 'in parallel', all variables
        // are available at once

        // let evaluates from left to right
        int i = 0;
        for(Pair<Expr, Expr> p : letRec.parallelDefs){

            code.addCode(codeV(p.second()));

            code.addInstruction(new Instr.Rewrite(n - i), stackDistance);

            // rewrite also modifies the stack pointer?
            stackDistance -= 1;
            i++;
        }

        code.addCode(codeV(letRec.inExpr));

        // we don't need the prev stack distance stuff
        addSlide(code, n);
        stackDistance -= n;

        environment = previous;

        addFallBackForCodeB(code, mode);

        return code;
    }

    @Override
    public Code visitUnOp(Expr.UnOp unOp, GenerationMode mode) {
        Code code = new Code();

        code.addCode(codeB(unOp.expr));

        switch(unOp.operator){
            case NOT:
                code.addInstruction(new Instr.Negate(), stackDistance);
                break;
            case MINUS:
                code.addInstruction(new Instr.FlipSign(), stackDistance);
                break;
            default:
                throw new RuntimeException(String.format("Unary operator '%s' not supported", unOp.operator));
        }

        // is fundamentally a codeB operation
        addFallBackForCodeV(code, mode);

        return code;
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
            case BinaryOperator.LEQ:
                code.addInstruction(new Instr.LessThanOrEqual(), stackDistance);
                break;
            case BinaryOperator.GEQ:
                code.addInstruction(new Instr.GreaterOrEqual(), stackDistance);
                break;
            case BinaryOperator.LESS:
                code.addInstruction(new Instr.Less(), stackDistance);
                break;
            case BinaryOperator.GREATER:
                code.addInstruction(new Instr.Greater(), stackDistance);
                break;
            case BinaryOperator.EQUAL:
                code.addInstruction(new Instr.Equal(), stackDistance);
                break;
            case BinaryOperator.MINUS:
                code.addInstruction(new Instr.Sub(), stackDistance);
                break;
            case BinaryOperator.AND:
                code.addInstruction(new Instr.And(), stackDistance);
                break;
            case BinaryOperator.OR:
                code.addInstruction(new Instr.Or(), stackDistance);
                break;
            case BinaryOperator.DIV:
                code.addInstruction(new Instr.Div(), stackDistance);
                break;
            case BinaryOperator.MOD:
                code.addInstruction(new Instr.Mod(), stackDistance);
                break;
            case BinaryOperator.UNEQUAL:
                code.addInstruction(new Instr.UnEqual(), stackDistance);
                break;
            default:
                throw new RuntimeException("Instruction not yet implemented.");
        }

        // one operator is consumed
        stackDistance -= 1;

        addFallBackForCodeV(code, mode);

        return code;
    }

    @Override
    public Code visitFunctionDefinition(Expr.FunctionDefinition functionDefinition, GenerationMode mode) {

        checkHasNoCodeB(mode, "A function definition returns a function, not a basic value");

        Code code = new Code();

        // first, gather all free variables (not function parameters, not locals)
        // and wrap them in a global vector

        // we add the parameters so they don't show up as the free variables
        // add the parameters to the address environment
        Environment previous = environment;
        Environment functionEnvironment = new Environment();

        functionEnvironment.insert(functionDefinition.variables, Visibility.L, 0, Index.DECREASING);

        Set<Expr> freeVariables = free(functionDefinition.rightHandSide, functionEnvironment);

        functionEnvironment.insert(freeVariables, Visibility.G, 0, Index.INCREASING);

        for(Expr var : freeVariables){

            //functionEnvironment.insert(var.varName, Visibility.G, globVars++);

            // with respect to the calling scope
            code.addCode(getVar((Expr.Variable) var));
        }

        int g = freeVariables.size();

        addMakeVec(code, g);

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

    private Set<Expr> free(Expr expr, Environment surroundingEnvironment){

        Set<Expr> freeVars = new HashSet<>();

        if(expr instanceof Expr.Variable){
            if(!surroundingEnvironment.env.containsKey(((Expr.Variable) expr).varName)){
                freeVars.add(expr);
            }
        }
        if(expr instanceof Expr.Cons){
            Set<Expr> freeVarsHead = free(((Expr.Cons) expr).head, surroundingEnvironment);
            Set<Expr> freeVarsTail = free(((Expr.Cons) expr).tail, surroundingEnvironment);
            freeVarsHead.addAll(freeVarsTail);

            return freeVarsHead;
        }
        if(expr instanceof Expr.Match){
            Set<Expr> freeVarsCond = free(((Expr.Match) expr).matchThis, surroundingEnvironment);
            Set<Expr> freeVarsNilMatch = free(((Expr.Match) expr).matchWithNil, surroundingEnvironment);

            // add h and t so they don't come up as free variables
            Environment surroundingCons = surroundingEnvironment.deepCopy();
            surroundingCons.insert(List.of(new Expr.Variable("h"), new Expr.Variable("t")), Visibility.L, 0, Index.INCREASING);

            Set<Expr> freeVarsConsMatch = free(((Expr.Match) expr).matchWithCons, surroundingCons);


            freeVarsCond.addAll(freeVarsNilMatch);
            freeVarsCond.addAll(freeVarsConsMatch);

            return freeVarsCond;
        }
        else if(expr instanceof Expr.UnOp){
            return free(((Expr.UnOp)expr).expr, surroundingEnvironment);
        }
        else if(expr instanceof Expr.BinOp){

            Set<Expr> freeVarsLeft = free(((Expr.BinOp) expr).left, surroundingEnvironment);
            Set<Expr> freeVarsRight = free(((Expr.BinOp) expr).right, surroundingEnvironment);
            freeVarsLeft.addAll(freeVarsRight);

            return freeVarsLeft;
        }
        else if (expr instanceof Expr.If){

            // there cannot be a variable definition in the condition of an if
            // don't allow: if (let a = 5) >= 1 then ...
            Set<Expr> freeVarsCond = free(((Expr.If) expr).condition, surroundingEnvironment);
            Set<Expr> freeVarsIf = free(((Expr.If) expr).ifBranchExpr, surroundingEnvironment);
            Set<Expr> freeVarsElse = free(((Expr.If) expr).elseBranchExpr, surroundingEnvironment);

            freeVars.addAll(freeVarsCond);
            freeVars.addAll(freeVarsIf);
            freeVars.addAll(freeVarsElse);
        }
        else if(expr instanceof Expr.FunctionDefinition){
            // the parameters are now defined, at least they are not free anymore
            Environment newSurrounding = surroundingEnvironment.deepCopy();

            newSurrounding.insert(((Expr.FunctionDefinition) expr).variables, Visibility.L, 0, Index.INCREASING);

            return free(((Expr.FunctionDefinition) expr).rightHandSide, newSurrounding);
        }
        else if(expr instanceof Expr.Let){
           Environment newSurrounding = surroundingEnvironment.deepCopy();

           // right hand side is known
           newSurrounding.insert(((Expr.Let) expr).target, Visibility.L, 0, Index.INCREASING);

           return free(((Expr.Let) expr).inExpr, newSurrounding);
        }
        else if(expr instanceof Expr.FunctionApplication){
            Set<Expr> freeFunction = free(((Expr.FunctionApplication) expr).functionExpr, surroundingEnvironment);
            freeVars.addAll(freeFunction);

            for(Expr argumentExpr : ((Expr.FunctionApplication) expr).exprArguments){
                freeVars.addAll(free(argumentExpr, surroundingEnvironment));
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

        int prevStackDistance = stackDistance;

        // reserve three spots for the organizational cells
        stackDistance += 3;

        // push all the arguments on the stack, start from right to left (that's why reversed)
        for(Expr exprArgument : functionApplication.exprArguments.reversed()){
            code.addCode(codeV(exprArgument));
        }

        code.addCode(codeV(functionApplication.functionExpr));

        code.addInstruction(new Instr.Apply(), stackDistance);

        // old stack distance + 1 for the return value
        stackDistance = prevStackDistance + 1;

        code.setJumpLabelAtEnd(continueAfterFunctionCall);

        addFallBackForCodeB(code, mode);

        return code;
    }

    @Override
    public Code visitIf(Expr.If ifExpr, GenerationMode mode) {

        Code code = new Code();
        String jumpOverIfBranch = code.getNewJumpLabel();
        String jumpOverElseBranch = code.getNewJumpLabel();

        code.addCode(codeB(ifExpr.condition));

        int stackDistBeforeConditional = stackDistance;

        code.addInstruction(new Instr.JumpZ(jumpOverIfBranch), stackDistance);
        stackDistance -= 1;

        int prevStackDistance = stackDistance;

        code.addCode(codeV(ifExpr.ifBranchExpr));
        code.addInstruction(new Instr.Jump(jumpOverElseBranch), stackDistance);

        // we have to reset because only if or else is going to influence the stack distance?
        stackDistance = prevStackDistance;

        code.addCode(codeV(ifExpr.elseBranchExpr), jumpOverIfBranch);

        code.setJumpLabelAtEnd(jumpOverElseBranch);

        stackDistance = stackDistBeforeConditional;

        // IMPORTANT (this could have done better by using codeB in both branches)
        addFallBackForCodeB(code, mode);

        return code;
    }

    private void checkHasNoCodeB(GenerationMode mode, String message){
        if(mode == GenerationMode.B){
            throw new RuntimeException(message);
        }
    }

    private void addFallBackForCodeB(Code code, GenerationMode mode){
        if(mode == GenerationMode.B){
            code.addInstruction(new Instr.GetBasic(), stackDistance);
        }
    }

    private void addFallBackForCodeV(Code code, GenerationMode mode){
        if(mode == GenerationMode.V){
            code.addInstruction(new Instr.MakeBasic(), stackDistance);
        }
    }
}
