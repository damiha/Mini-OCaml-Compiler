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

        Expr expr = new Expr.LetRec(
                List.of(
                        new Pair<>(
                                new Expr.Variable("f"),
                                new Expr.FunctionDefinition(List.of(new Expr.Variable("x"), new Expr.Variable("y")),
                                        new Expr.If(new Expr.BinOp(new Expr.Variable("y"), BinaryOperator.LEQ, new Expr.IntLiteral(1)),
                                                    new Expr.Variable("y"),
                                                    new Expr.FunctionApplication(
                                                            new Expr.Variable("f"),
                                                            List.of(
                                                                    new Expr.BinOp(new Expr.Variable("x"), BinaryOperator.MUL, new Expr.Variable(("y"))),
                                                                    new Expr.BinOp(new Expr.Variable("y"), BinaryOperator.MINUS, new Expr.IntLiteral(1))
                                                            )
                                                    )
                                                )
                                        )
                        )
                ),
                new Expr.FunctionApplication(new Expr.Variable("f"), List.of(new Expr.IntLiteral(1)))
        );

        Compiler compiler = new Compiler();
        compiler.stackDistance = 0;

        Code code = compiler.codeV(expr);

        System.out.println(code);
    }
}