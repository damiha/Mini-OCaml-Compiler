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
                new Expr.Variable("a"),
                new Expr.IntLiteral(1),
                new Expr.FunctionDefinition(
                        "f",
                        List.of(new Expr.Variable("b")),
                        new Expr.BinOp(new Expr.Variable("a"), BinaryOperator.PLUS, new Expr.Variable("b"))
                )
        );

        Compiler compiler = new Compiler();
        compiler.stackDistance = 0;

        Code code = compiler.codeV(expr);

        System.out.println(code);
    }
}