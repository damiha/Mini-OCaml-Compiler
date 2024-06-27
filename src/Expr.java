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

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitIf(this, mode);
        }
    }

    static class FunctionApplication extends Expr{

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitFunctionApplication(this, mode);
        }
    }

    static class FunctionDefinition extends Expr{

        @Override
        <T> T accept(Visitor<T> visitor, GenerationMode mode) {
            return visitor.visitFunctionDefinition(this, mode);
        }
    }

    static class Let extends Expr{

        // let x_1 = e_1 in e_0
        // x_1 is the target
        // e_1 is the right hand side
        // e_0 is the inExpr
        Variable target;
        Expr rightHandSide;
        Expr inExpr;

        public Let(Variable target, Expr rightHandSide, Expr inExpr){
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
    }
}
