import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

    @Test
    public void testNestedLet(){


        List<Token> tokens = List.of(
                new Token(0, TokenType.LET, "let"),
                new Token(0, TokenType.IDENTIFIER, "a"),
                new Token(0, TokenType.EQUAL, "="),
                new Token(0, TokenType.NUMBER, "19", (Integer)19),
                new Token(0, TokenType.IN, "in"),
                new Token(0, TokenType.LET, "let"),
                new Token(0, TokenType.IDENTIFIER, "b"),
                new Token(0, TokenType.EQUAL, "="),
                new Token(0, TokenType.IDENTIFIER, "a"),
                new Token(0, TokenType.STAR, "*"),
                new Token(0, TokenType.IDENTIFIER, "a"),
                new Token(0, TokenType.IN, "in"),
                new Token(0, TokenType.IDENTIFIER, "a"),
                new Token(0, TokenType.PLUS, "+"),
                new Token(0, TokenType.IDENTIFIER, "b"),
                new Token(0, TokenType.EOF, "")
        );

        Parser parser = new Parser(tokens);

        Expr expr = parser.parse();

        Expr expected = new Expr.Let(
                new Expr.Variable("a"),
                new Expr.IntLiteral(19),
                new Expr.Let(new Expr.Variable("b"),
                        new Expr.BinOp(new Expr.Variable("a"), BinaryOperator.MUL, new Expr.Variable("a")),
                        new Expr.BinOp(new Expr.Variable("a"), BinaryOperator.PLUS, new Expr.Variable("b"))
                )
        );

        assertEquals(expected, expr);
    }

    @Test
    public void testFunctionDefinition(){

        // let a = 1 in fun b -> a + b

        List<Token> tokens = List.of(
                new Token(0, TokenType.LET, "let"),
                new Token(0, TokenType.IDENTIFIER, "a"),
                new Token(0, TokenType.EQUAL, "="),
                new Token(0, TokenType.NUMBER, "1", (Integer)1),
                new Token(0, TokenType.IN, "in"),
                new Token(0, TokenType.FUN, "fun"),
                new Token(0, TokenType.IDENTIFIER, "b"),
                new Token(0, TokenType.ARROW, "->"),
                new Token(0, TokenType.IDENTIFIER, "a"),
                new Token(0, TokenType.PLUS, "+"),
                new Token(0, TokenType.IDENTIFIER, "b"),
                new Token(0, TokenType.EOF, "")
        );

        Parser parser = new Parser(tokens);

        Expr expr = parser.parse();

        Expr expected = new Expr.Let(
                new Expr.Variable("a"),
                new Expr.IntLiteral(1),
                new Expr.FunctionDefinition(
                        List.of(new Expr.Variable("b")),
                        new Expr.BinOp(new Expr.Variable("a"), BinaryOperator.PLUS, new Expr.Variable("b"))
                )
        );

        assertEquals(expected, expr);
    }

    @Test
    public void testFunctionApplication1(){

        // let a = 17 in let f = fun b -> a + b in f 42

        List<Token> tokens = List.of(
                new Token(0, TokenType.LET, "let"),
                new Token(0, TokenType.IDENTIFIER, "a"),
                new Token(0, TokenType.EQUAL, "="),
                new Token(0, TokenType.NUMBER, "17", (Integer)17),
                new Token(0, TokenType.IN, "in"),
                new Token(0, TokenType.LET, "let"),
                new Token(0, TokenType.IDENTIFIER, "f"),
                new Token(0, TokenType.EQUAL, "="),
                new Token(0, TokenType.FUN, "fun"),
                new Token(0, TokenType.IDENTIFIER, "b"),
                new Token(0, TokenType.ARROW, "->"),
                new Token(0, TokenType.IDENTIFIER, "a"),
                new Token(0, TokenType.PLUS, "+"),
                new Token(0, TokenType.IDENTIFIER, "b"),
                new Token(0, TokenType.IN, "in"),
                new Token(0, TokenType.IDENTIFIER, "f"),
                new Token(0, TokenType.NUMBER, "42", (Integer)42),
                new Token(0, TokenType.EOF, "")
        );

        Parser parser = new Parser(tokens);

        Expr expr = parser.parse();

        Expr expected = new Expr.Let(
                new Expr.Variable("a"),
                new Expr.IntLiteral(17),
                new Expr.Let(
                        new Expr.Variable("f"),
                        new Expr.FunctionDefinition(
                                List.of(new Expr.Variable("b")),
                                new Expr.BinOp(new Expr.Variable("a"), BinaryOperator.PLUS, new Expr.Variable("b"))
                        ),
                        new Expr.FunctionApplication(new Expr.Variable("f"), List.of(new Expr.IntLiteral(42)))
                )
        );

        assertEquals(expected, expr);
    }

    @Test
    public void testLetRec1(){


        // let rec f = fun x y → if y ≤ 1 then x else f ( x ∗ y )( y − 1 ) in f 1

        List<Token> tokens = List.of(
                new Token(0, TokenType.LET, "let"),
                new Token(0, TokenType.REC, "rec"),
                new Token(0, TokenType.IDENTIFIER, "f"),
                new Token(0, TokenType.EQUAL, "="),
                new Token(0, TokenType.FUN, "fun"),
                new Token(0, TokenType.IDENTIFIER, "x"),
                new Token(0, TokenType.IDENTIFIER, "y"),
                new Token(0, TokenType.ARROW, "->"),
                new Token(0, TokenType.IF, "if"),
                new Token(0, TokenType.IDENTIFIER, "y"),
                new Token(0, TokenType.LESS_EQUAL, "<="),
                new Token(0, TokenType.NUMBER, "1", (Integer)1),
                new Token(0, TokenType.THEN, "then"),
                new Token(0, TokenType.IDENTIFIER, "x"),
                new Token(0, TokenType.ELSE, "else"),
                new Token(0, TokenType.IDENTIFIER, "f"),
                new Token(0, TokenType.LEFT_PAREN, "("),
                new Token(0, TokenType.IDENTIFIER, "x"),
                new Token(0, TokenType.STAR, "*"),
                new Token(0, TokenType.IDENTIFIER, "y"),
                new Token(0, TokenType.RIGHT_PAREN, ")"),
                new Token(0, TokenType.LEFT_PAREN, "("),
                new Token(0, TokenType.IDENTIFIER, "y"),
                new Token(0, TokenType.MINUS, "-"),
                new Token(0, TokenType.NUMBER, "1", (Integer)1),
                new Token(0, TokenType.RIGHT_PAREN, ")"),
                new Token(0, TokenType.IN, "in"),
                new Token(0, TokenType.IDENTIFIER, "f"),
                new Token(0, TokenType.NUMBER, "1", (Integer)1),
                new Token(0, TokenType.EOF, "")
        );

        Parser parser = new Parser(tokens);

        Expr expr = parser.parse();

        Expr expected = new Expr.LetRec(
                List.of(
                        new Pair<>(
                                new Expr.Variable("f"),
                                new Expr.FunctionDefinition(List.of(new Expr.Variable("x"), new Expr.Variable("y")),
                                        new Expr.If(new Expr.BinOp(new Expr.Variable("y"), BinaryOperator.LEQ, new Expr.IntLiteral(1)),
                                                new Expr.Variable("x"),
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

        assertEquals(expected, expr);
    }
}