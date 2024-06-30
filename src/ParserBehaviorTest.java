
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParserBehaviorTest {

    // skips the lexer so take this with a grain of salt
    @Test
    public void testOverSupply1(){
        String source = "let f = fun x y -> (fun z -> x + y + z) in f 10 20 30";

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.getTokens());

        Expr parsed = parser.parse();

        // TODO: add test
        // parser result looks normal for now
        System.out.println("wtf");
    }

    @Test
    public void testLetRec1(){
        String source = "let rec f = fun x y -> if y <= 1 then x else f ( x * y ) ( y - 1 ) in f 1 3";

        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer.getTokens());

        Expr parsed = parser.parse();

        // TODO: add test
        // parser result looks normal for now
        System.out.println("wtf");
    }
}
