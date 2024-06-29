
import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BehaviorTest {

    @Test
    public void testNestedLet(){

        String source = "let a = 19 in let b = a * a in a + b";

        Runner runner = new Runner(source);

        int result = runner.run();

        // 19 + (19 * 19) = 380
        assertEquals(result, 380);
    }

    @Test
    public void testFunctionApplication1(){

        String source = "let a = 17 in let f = fun b -> a + b in f 42";

        Runner runner = new Runner(source);

        int result = runner.run();

        // 42 + 17 = 59
        assertEquals(result, 59);
    }

    @Test
    public void testUnderSupply1(){

        String source = "let f = fun x y -> x + y in let f2 = f 10 in f2 100";

        Runner runner = new Runner(source);

        int result = runner.run();

        // 10 + 100
        assertEquals(result, 110);
    }

    @Test
    public void testOverSupply1(){

        String source = "let f = fun x y -> (fun z -> x + y + z) in f 10 20 30";

        Runner runner = new Runner(source);

        int result = runner.run();

        // 10 + 20 + 30
        assertEquals(result, 60);
    }

    @Test
    public void testLetRec0(){

        // this should calculate the factorial function
        String source = "let rec a = 3 in a";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(3, result);
    }

    @Test
    public void testLet2(){

        // this should calculate the factorial function
        String source = "let rec f = fun x y -> if y <= 5 then x else 16 in f 3 2";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(3, result);
    }

    @Test
    public void testLetRec1(){

        // this should calculate the factorial function
        String source = "let rec f = fun x y -> if y <= 1 then x else f ( x * y ) ( y - 1 ) in f 1 3";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(result, 6);
    }
}
