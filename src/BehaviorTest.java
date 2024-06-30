
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
    public void tupleUnpacking1(){

        String source = "let (a, b) = (5, 3) in a + b";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(result, 8);
    }

    @Test
    public void tupleUnpacking2(){

        String source = "let (a, b, c) = (5, 3, 8) in a + b + c";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(result, 16);
    }

    @Test
    public void nestedTupleUnpacking1(){

        String source = "let (a, b) = (5, 3) in let (c, d) = (8, 11) in a + b + c + d";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(result, 27);
    }

    @Test
    public void tupleAndFunctions1(){

        String source = "let f = fun p -> (let (x, y) = p in x + y) in f (1, 3)";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(result, 4);
    }

    @Test
    public void tupleAccess1(){

        String source = "let f = (fun p -> #2 p) in f (1, 3, 5, 7)";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(result, 5);
    }

    // RECURSIVE TUPLE UNPACKING DOESN'T WORK YET

    // unpack a tuple that is returned?
    @Test
    public void tupleAndFunctions2(){

        String source = "let f = (fun x -> (1, 3)) in let (a, b) = f 0 in a + b";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(result, 4);
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
    public void testLetRec01(){

        // this should calculate the factorial function
        String source = "let rec a = 3 and b = 5 in a + b";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(8, result);
    }

    @Test
    public void testIfAdd(){

        // this should calculate the factorial function
        String source = "let x = 3 in let y = 4 in 1 + (if x <= y then x else y) + x";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(7, result);
    }

    @Test
    public void testLetRec1(){

        // this should calculate the factorial function
        String source = "let rec f = fun x y -> if y <= 1 then x else f ( x * y ) ( y - 1 ) in f 1 3";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(result, 6);
    }

    @Test
    public void testMutRec1(){

        // this should calculate the factorial function
        String source = "let rec even = (fun e -> if e == 0 then 1 else odd (e - 1)) and odd = (fun o -> if o == 0 then 0 else 1 - even(o - 1)) in even 77";

        Runner runner = new Runner(source);

        int result = runner.run();

        assertEquals(result, 0);
    }

}
