import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class Expr {

    abstract <T> T accept(Visitor<T> visitor, GenerationMode mode);

    public abstract boolean equals(Object other);

    static class IntLiteral extends Expr{

        int value;

        public IntLiteral(int value){
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitIntLiteral(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof IntLiteral) && (((IntLiteral) other).value == value);
        }
    }

    static class Variable extends Expr{

        String varName;

        public Variable(String varName){
            this.varName = varName;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitVariable(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof Variable && ((Variable) other).varName.equals(varName));
        }
    }

    static class UnOp extends Expr{

        Expr expr;
        UnaryOperator operator;

        public UnOp(UnaryOperator operator, Expr expr){
            this.operator = operator;
            this.expr = expr;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitUnOp(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof UnOp) && (((UnOp) other).operator == operator) && (((UnOp) other).expr.equals(expr));
        }
    }

    static class BinOp extends Expr{
        BinaryOperator operator;
        Expr left;
        Expr right;

        public BinOp(Expr left, BinaryOperator operator, Expr right){
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitBinOp(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof BinOp) && (((BinOp) other).operator == operator) &&
                    (((BinOp) other).left.equals(left)) && (((BinOp) other).right.equals(right));
        }
    }

    static class If extends Expr{

        Expr condition;
        Expr ifBranchExpr;
        Expr elseBranchExpr;

        public If(Expr condition, Expr ifBranchExpr, Expr elseBranchExpr){
            this.condition = condition;
            this.ifBranchExpr = ifBranchExpr;
            this.elseBranchExpr = elseBranchExpr;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitIf(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof If) && (((If) other).condition.equals(condition)) &&
                    ((If) other).ifBranchExpr.equals(ifBranchExpr) &&
                    ((If) other).elseBranchExpr.equals(elseBranchExpr);
        }
    }

    static class FunctionApplication extends Expr{

        Expr functionExpr;

        List<Expr> exprArguments;

        public FunctionApplication(Expr functionExpr, List<Expr> exprArguments){
            this.functionExpr = functionExpr;
            this.exprArguments = new ArrayList<>(exprArguments);
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitFunctionApplication(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            if(!(other instanceof FunctionApplication)){
                return false;
            }

            // number of arguments must match
            if(((FunctionApplication) other).exprArguments.size() != exprArguments.size()){
                return false;
            }

            if(!functionExpr.equals(((FunctionApplication) other).functionExpr)){
                return false;
            }

            // check that all arguments match
            int i = 0;
            for(Expr otherArg : ((FunctionApplication) other).exprArguments){
                if(!exprArguments.get(i).equals(otherArg)){
                    return false;
                }
                i++;
            }
            return true;
        }
    }

    static class Tuple extends Expr{

        List<Expr> expressions;

        public Tuple(List<Expr> expressions){
            this.expressions = new ArrayList<>(expressions);
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitTuple(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            if(!(other instanceof Tuple)){
                return false;
            }


            // number of arguments must match
            if(((Tuple) other).expressions.size() != expressions.size()){
                return false;
            }

            // check that all arguments match
            int i = 0;
            for(Expr otherArg : ((Tuple) other).expressions){
                if(!expressions.get(i).equals(otherArg)){
                    return false;
                }
                i++;
            }
            return true;
        }
    }

    static class TupleAccess extends Expr{
        Expr expr;

        // index
        int j;

        public TupleAccess(Expr expr, int j){
            this.expr = expr;
            this.j = j;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitTupleAccess(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof TupleAccess) &&
                    ((TupleAccess) other).expr.equals(expr) &&
                    (((TupleAccess) other).j) == j;
        }
    }

    // this returns a function as a value (functions as variables you can pass around)

    static class FunctionDefinition extends Expr{

        List<Expr> variables;

        Expr rightHandSide;

        public FunctionDefinition(List<Expr> variables, Expr rightHandSide){
            this.variables = new ArrayList<>(variables);
            this.rightHandSide = rightHandSide;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitFunctionDefinition(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            if(!(other instanceof FunctionDefinition)){
                return false;
            }

            // number of arguments must match
            if(((FunctionDefinition) other).variables.size() != variables.size()){
                return false;
            }

            if(!rightHandSide.equals(((FunctionDefinition) other).rightHandSide)){
                return false;
            }

            // check that all arguments match
            int i = 0;
            for(Expr otherArg : ((FunctionDefinition) other).variables){
                if(!variables.get(i).equals(otherArg)){
                    return false;
                }
                i++;
            }
            return true;
        }
    }

    static class Nil extends Expr{

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitNilExpr(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            throw new RuntimeException("'equals' not implemented for list");
        }
    }

    static class Cons extends Expr{

        Expr head;
        Expr tail;

        public Cons(Expr head, Expr tail){
            this.head = head;
            this.tail = tail;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitConsExpr(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            throw new RuntimeException("'equals' not implemented for list");
        }
    }

    static class Let extends Expr{

        // let x_1 = e_1 in e_0
        // x_1 is the target (can also be a tuple of variables)
        // e_1 is the right hand side
        // e_0 is the inExpr
        Expr target;
        Expr rightHandSide;
        Expr inExpr;

        public Let(Expr target, Expr rightHandSide, Expr inExpr){
            this.target = target;
            this.rightHandSide = rightHandSide;
            this.inExpr = inExpr;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitLet(this, mode);
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof Let) &&
                    ((Let) other).target.equals(target) &&
                    ((Let) other).rightHandSide.equals(rightHandSide) &&
                    ((Let) other).inExpr.equals(inExpr);
        }
    }

    static class LetRec extends Expr{

        List<Pair<Expr, Expr>> parallelDefs;
        Expr inExpr;

        public LetRec(List<Pair<Expr, Expr>> parallelDefs, Expr inExpr){
            this.parallelDefs = new ArrayList<>(parallelDefs);
            this.inExpr = inExpr;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitLetRec(this, mode);
        }

        @Override
        public boolean equals(Object other) {

            if(!(other instanceof LetRec)){
                return false;
            }

            if(!(((LetRec) other).inExpr.equals(inExpr))){
                return false;
            }

            // must have same number of parallel definitions
            if(!(((LetRec) other).parallelDefs.size() == parallelDefs.size())){
                return false;
            }


            // now we can check if every pair matches
            int i = 0;
            for(Pair<Expr, Expr> p : parallelDefs){

                // pairs are Java records,
                // and they get compared by calling equals on first and second component?
                if(!((LetRec) other).parallelDefs.get(i).equals(p)){
                    return false;
                }
            }
            return true;
        }
    }

    interface Visitor<T> {
        T visitLet(Let let, GenerationMode mode);
        T visitLetRec(LetRec letRec, GenerationMode mode);
        T visitFunctionDefinition(FunctionDefinition functionDefinition, GenerationMode mode);
        T visitFunctionApplication(FunctionApplication functionApplication, GenerationMode mode);
        T visitIf(If ifExpr, GenerationMode mode);
        T visitBinOp(BinOp binOp, GenerationMode mode);
        T visitUnOp(UnOp unOp, GenerationMode mode);
        T visitIntLiteral(IntLiteral intLiteral, GenerationMode mode);
        T visitVariable(Variable variable, GenerationMode mode);
        T visitTuple(Tuple tuple, GenerationMode mode);
        T visitTupleAccess(TupleAccess tupleAccess, GenerationMode mode);
        T visitNilExpr(Nil nil, GenerationMode mode);
        T visitConsExpr(Cons cons, GenerationMode mode);
    }
}
