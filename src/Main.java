import java.util.List;

public class Main {
    public static void main(String[] args) {

        /*
        Expr expr = new Expr.IntLiteral(19);
         */

        // test global vars
        /*
        Compiler compiler = new Compiler();

        compiler.environment.insert("x", Visibility.G, 1);

        Expr expr = new Expr.Variable("x");
         */

        Expr expr = new Expr.Let(
                new Expr.Variable("f"),
                new Expr.FunctionDefinition(
                        List.of(
                                new Expr.Variable("x"),
                                new Expr.Variable("y")),
                                    new Expr.Let(new Expr.Variable("a"), new Expr.IntLiteral(5),
                                    new Expr.Let(new Expr.Variable("b"),
                                                    new Expr.BinOp(new Expr.Variable("x"), BinaryOperator.PLUS,
                                                    new Expr.BinOp(new Expr.IntLiteral(2), BinaryOperator.MUL, new Expr.Variable("y"))
                                        ), new Expr.BinOp(new Expr.Variable("b"), BinaryOperator.PLUS, new Expr.BinOp(new Expr.Variable("a"), BinaryOperator.MUL, new Expr.Variable("x"))))
                )),
                new Expr.FunctionApplication(new Expr.Variable("f"), List.of(new Expr.IntLiteral(0), new Expr.IntLiteral(1)))
        );

        Compiler compiler = new Compiler();
        compiler.stackDistance = 0;

        Code code = compiler.codeV(expr);

        System.out.println(code);
    }
}