import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LexerTest {

    @Test
    public void testNestedLet(){
        String source = "let a = 19 in let b = a * a in a + b";

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.getTokens();

        List<Token> expected = new ArrayList<>();

        expected.add(new Token(0, TokenType.LET, "let"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "a"));
        expected.add(new Token(0, TokenType.EQUAL, "="));
        expected.add(new Token(0, TokenType.NUMBER, "19"));
        expected.add(new Token(0, TokenType.IN, "in"));
        expected.add(new Token(0, TokenType.LET, "let"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "b"));
        expected.add(new Token(0, TokenType.EQUAL, "="));
        expected.add(new Token(0, TokenType.IDENTIFIER, "a"));
        expected.add(new Token(0, TokenType.STAR, "*"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "a"));
        expected.add(new Token(0, TokenType.IN, "in"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "a"));
        expected.add(new Token(0, TokenType.PLUS, "+"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "b"));
        expected.add(new Token(0, TokenType.EOF, ""));

        assertEquals(expected.toString(), tokens.toString());
    }

    @Test
    public void testFunctionDefinition(){
        String source = "fun b -> a + b";

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.getTokens();

        List<Token> expected = new ArrayList<>();

        expected.add(new Token(0, TokenType.FUN, "fun"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "b"));
        expected.add(new Token(0, TokenType.ARROW, "->"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "a"));
        expected.add(new Token(0, TokenType.PLUS, "+"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "b"));
        expected.add(new Token(0, TokenType.EOF, ""));

        assertEquals(expected.toString(), tokens.toString());
    }

    @Test
    public void testLetRec(){
        String source = "let rec f = fun x y -> if y <= 1 then x else f ( x * y )( y - 1 ) in f 1";

        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.getTokens();

        List<Token> expected = new ArrayList<>();

        expected.add(new Token(0, TokenType.LET, "let"));
        expected.add(new Token(0, TokenType.REC, "rec"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "f"));
        expected.add(new Token(0, TokenType.EQUAL, "="));
        expected.add(new Token(0, TokenType.FUN, "fun"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "x"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "y"));
        expected.add(new Token(0, TokenType.ARROW, "->"));
        expected.add(new Token(0, TokenType.IF, "if"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "y"));
        expected.add(new Token(0, TokenType.LESS_EQUAL, "<="));
        expected.add(new Token(0, TokenType.NUMBER, "1"));
        expected.add(new Token(0, TokenType.THEN, "then"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "x"));
        expected.add(new Token(0, TokenType.ELSE, "else"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "f"));
        expected.add(new Token(0, TokenType.LEFT_PAREN, "("));
        expected.add(new Token(0, TokenType.IDENTIFIER, "x"));
        expected.add(new Token(0, TokenType.STAR, "*"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "y"));
        expected.add(new Token(0, TokenType.RIGHT_PAREN, ")"));
        expected.add(new Token(0, TokenType.LEFT_PAREN, "("));
        expected.add(new Token(0, TokenType.IDENTIFIER, "y"));
        expected.add(new Token(0, TokenType.MINUS, "-"));
        expected.add(new Token(0, TokenType.NUMBER, "1"));
        expected.add(new Token(0, TokenType.RIGHT_PAREN, ")"));
        expected.add(new Token(0, TokenType.IN, "in"));
        expected.add(new Token(0, TokenType.IDENTIFIER, "f"));
        expected.add(new Token(0, TokenType.NUMBER, "1"));
        expected.add(new Token(0, TokenType.EOF, ""));

        assertEquals(expected.toString(), tokens.toString());
    }

}