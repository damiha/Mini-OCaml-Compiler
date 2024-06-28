import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class Expr {

    abstract <T> T accept(Visitor<T> visitor, GenerationMode mode);

    static class IntLiteral extends Expr{

        int value;

        public IntLiteral(int value){
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitIntLiteral(this, mode);
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
    }
}
