
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BehaviorTest {

    @Test
    public void testNestedLet(){

        String source = "let a = 19 in let b = a * a in a + b";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        // 19 + (19 * 19) = 380
        assertEquals(result, 380);
    }

    @Test
    public void testAnd1(){

        // True and False
        String source = "(28 && (0 == 1))";

        Runner runner = new Runner();

        assertEquals("0", runner.getOutput(source));
    }

    @Test
    public void testAnd2(){

        // True and False
        String source = "((0 == 0) && 29)";

        Runner runner = new Runner();

        assertEquals("1", runner.getOutput(source));
    }

    @Test
    public void testGeq1(){

        String source = "27 >= 27";

        Runner runner = new Runner();

        assertEquals("1", runner.getOutput(source));
    }

    @Test
    public void testGeq2(){

        // True and False
        String source = "26 >= 27";

        Runner runner = new Runner();

        assertEquals("0", runner.getOutput(source));
    }

    @Test
    public void testLess1(){

        String source = "27 < 28";

        Runner runner = new Runner();

        assertEquals("1", runner.getOutput(source));
    }

    @Test
    public void testLess2(){

        // True and False
        String source = "27 < 27";

        Runner runner = new Runner();

        assertEquals("0", runner.getOutput(source));
    }

    @Test
    public void testGreater1(){

        String source = "28 > 27";

        Runner runner = new Runner();

        assertEquals("1", runner.getOutput(source));
    }

    @Test
    public void testGreater2(){

        // True and False
        String source = "28 > 28";

        Runner runner = new Runner();

        assertEquals("0", runner.getOutput(source));
    }

    @Test
    public void testDiv(){

        String source = "28 / 7";

        Runner runner = new Runner();

        assertEquals("4", runner.getOutput(source));
    }

    @Test
    public void testNot1(){

        // True and False
        String source = "!(28 == 28)";

        Runner runner = new Runner();

        assertEquals("0", runner.getOutput(source));
    }

    @Test
    public void testNot2(){

        String source = "!(28 == 29)";

        Runner runner = new Runner();

        assertEquals("1", runner.getOutput(source));
    }

    @Test
    public void testUnMinus1(){

        // True and False
        String source = "(-5 == -2 - 4)";

        Runner runner = new Runner();

        assertEquals("0", runner.getOutput(source));
    }

    @Test
    public void testUnMinus2(){

        String source = "(-5 == -2 - 3)";

        Runner runner = new Runner();

        assertEquals("1", runner.getOutput(source));
    }

    @Test
    public void testMod(){

        String source = "29 % 3";

        Runner runner = new Runner();

        assertEquals("2", runner.getOutput(source));
    }

    @Test
    public void testUnequal1(){

        String source = "29 != 3";

        Runner runner = new Runner();

        assertEquals("1", runner.getOutput(source));
    }

    @Test
    public void testUnequal2(){

        // True and False
        String source = "3 != 3";

        Runner runner = new Runner();

        assertEquals("0", runner.getOutput(source));
    }

    @Test
    public void testOr1(){

        // False or False
        String source = "((28 == 29) || (0 == 1))";

        Runner runner = new Runner();

        assertEquals("0", runner.getOutput(source));
    }

    @Test
    public void testOr2(){

        // False or true
        String source = "((0 == 1) || 29)";

        Runner runner = new Runner();

        assertEquals("1", runner.getOutput(source));
    }

    @Test
    public void testList1(){

        String source = "[]";

        Runner runner = new Runner();

        assertEquals(runner.getOutput(source), "[]");
    }



    @Test
    public void testList2(){

        String source = "1::2::3::[]";

        Runner runner = new Runner();

        assertEquals(runner.getOutput(source), "1::2::3::[]");
    }

    // list and let
    @Test
    public void testList3(){

        String source = "let (a, b) = (5, 3) in a::b::[]";

        Runner runner = new Runner();

        assertEquals(runner.getOutput(source), "5::3::[]");
    }

    // functions and let
    @Test
    public void testList4(){

        String source = "let f = (fun x y -> x * y) in (f 5 3)::[]";

        Runner runner = new Runner();

        assertEquals(runner.getOutput(source), "15::[]");
    }

    // functions and let
    @Test
    public void testList4_1(){

        String source = "let f = (fun x y -> x::y::[]) in f 5 3";

        Runner runner = new Runner();

        assertEquals(runner.getOutput(source), "5::3::[]");
    }

    // list creation with recursion
    @Test
    public void testList5(){

        String source = "let rec f = (fun x -> if x == 0 then [] else x::(f (x-1))) in f 3";

        Runner runner = new Runner();

        assertEquals(runner.getOutput(source), "3::2::1::[]");
    }

    @Test
    public void testList5_1(){

        String source = "let rec f = (fun x l-> if x == 0 then l else f (x-1) (x::l)) in f 3 []";

        Runner runner = new Runner();

        assertEquals("1::2::3::[]", runner.getOutput(source));
    }

    // under supply and lists
    @Test
    public void testList6(){

        String source = "let f = (fun x y -> x::y::[]) in let first_one = (fun a -> f 1 a) in first_one 5";

        Runner runner = new Runner();

        assertEquals("1::5::[]", runner.getOutput(source));
    }

    @Test
    public void testList6_1(){

        String source = "let f = (fun x y -> x::y::[]) in let last_one = (fun a -> f a 1) in last_one 5";

        Runner runner = new Runner();

        assertEquals("5::1::[]", runner.getOutput(source));
    }

    // over supply and lists
    @Test
    public void testList6_2(){

        String source = "let f = (fun x -> fun y z -> x :: y :: z :: []) in f 1 2 3";

        Runner runner = new Runner();

        assertEquals("1::2::3::[]", runner.getOutput(source));
    }

    @Test
    public void testMatchExpression1(){

        String source = "match [] with [] -> 1 | h :: t -> 2";

        Runner runner = new Runner();

        assertEquals("1", runner.getOutput(source));
    }

    @Test
    public void testMatchExpression2(){

        String source = "match 1::[] with [] -> 1 | h :: t -> 2";

        Runner runner = new Runner();

        assertEquals("2", runner.getOutput(source));
    }

    @Test
    public void testMatchExpression3(){

        String source = "match 15::[] with [] -> 1 | h :: t -> h";

        Runner runner = new Runner();

        assertEquals("15", runner.getOutput(source));
    }

    @Test
    public void testMatchExpression4(){

        String source = "match 15::(2::[]) with [] -> 1 | h :: t -> 3::t";

        Runner runner = new Runner();

        assertEquals("3::2::[]", runner.getOutput(source));
    }

    @Test
    public void testMatchExpression5(){

        String source = "let rec app = (fun x l -> match l with [] -> x::[] | h::t -> h::(app x t)) in app 5 (1::2::[])";

        Runner runner = new Runner();

        assertEquals("1::2::5::[]", runner.getOutput(source));
    }

    @Test
    public void testConcat(){

        String source = "let rec concat = (fun l1 l2 -> match l1 with [] -> l2 | h::t -> h :: (concat t l2)) in concat (1::2::[]) (3::4::[])";

        Runner runner = new Runner();

        assertEquals("1::2::3::4::[]", runner.getOutput(source));
    }

    @Test
    public void testRev(){

        String source = """
                let rec app = (fun x l -> match l with [] -> x::[] | h::t -> h::(app x t)) in
                let rec rev = (fun l -> match l with [] -> [] | h :: t -> app h (rev t)) in
                rev (1::2::3::[])
                """;

        Runner runner = new Runner();

        assertEquals("3::2::1::[]", runner.getOutput(source));
    }

    @Test
    public void testInsert(){

        String source = """
                let rec insert = (fun x l -> match l with [] -> x::[] | h :: t -> (if x <= h then x::h::t else h::(insert x t)))
                in insert 3 (1::4::7::[])
                """;

        Runner runner = new Runner();

        assertEquals("1::3::4::7::[]", runner.getOutput(source));
    }

    @Test
    public void testInsort(){

        String source = """
                let rec insert = (fun x l -> match l with [] -> x::[] | h :: t -> (if x <= h then x::h::t else h::(insert x t))) in
                let rec insort = (fun l -> match l with [] -> [] | h :: t -> insert h (insort t)) in
                insort (1::5::3::4::2::[])
                """;

        Runner runner = new Runner();

        assertEquals("1::2::3::4::5::[]", runner.getOutput(source));
    }

    @Test
    public void testFibonacci(){

        String source = """
                let rec fib = fun n -> if n == 0 then 0 else if n == 1 then 1 else (fib (n-1) + fib (n-2))
                in fib 11
                """;

        Runner runner = new Runner();

        assertEquals("89", runner.getOutput(source));
    }

    @Test
    public void tupleUnpacking1(){

        String source = "let (a, b) = (5, 3) in a + b";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(result, 8);
    }

    @Test
    public void tupleUnpacking2(){

        String source = "let (a, b, c) = (5, 3, 8) in a + b + c";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(result, 16);
    }

    @Test
    public void nestedTupleUnpacking1(){

        String source = "let (a, b) = (5, 3) in let (c, d) = (8, 11) in a + b + c + d";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(result, 27);
    }

    @Test
    public void tupleAndFunctions1(){

        String source = "let f = fun p -> (let (x, y) = p in x + y) in f (1, 3)";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(result, 4);
    }

    // return functions as tuples?
    @Test
    public void tupleAndFunctions3(){

        String source = "let (f1, f2) = (fun x -> x + 1, fun y -> y + 2) in (f1 2)  + (f2 3)";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        // (2 + 1) + (3 + 2)
        assertEquals(result, 8);
    }

    @Test
    public void tupleAccess1(){

        String source = "let f = (fun p -> #2 p) in f (1, 3, 5, 7)";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(result, 5);
    }

    // RECURSIVE TUPLE UNPACKING DOESN'T WORK YET

    // unpack a tuple that is returned?
    @Test
    public void tupleAndFunctions2(){

        String source = "let f = (fun x -> (1, 3)) in let (a, b) = f 0 in a + b";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(result, 4);
    }

    @Test
    public void testFunctionApplication1(){

        String source = "let a = 17 in let f = fun b -> a + b in f 42";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        // 42 + 17 = 59
        assertEquals(result, 59);
    }

    @Test
    public void testUnderSupply1(){

        String source = "let f = fun x y -> x + y in let f2 = f 10 in f2 100";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        // 10 + 100
        assertEquals(result, 110);
    }

    @Test
    public void testOverSupply1(){

        String source = "let f = fun x y -> (fun z -> x + y + z) in f 10 20 30";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        // 10 + 20 + 30
        assertEquals(result, 60);
    }

    @Test
    public void testLetRec0(){

        // this should calculate the factorial function
        String source = "let rec a = 3 in a";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(3, result);
    }

    @Test
    public void testLetRec01(){

        // this should calculate the factorial function
        String source = "let rec a = 3 and b = 5 in a + b";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(8, result);
    }

    @Test
    public void testIfAdd(){

        // this should calculate the factorial function
        String source = "let x = 3 in let y = 4 in 1 + (if x <= y then x else y) + x";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(7, result);
    }

    @Test
    public void testLetRec1(){

        // this should calculate the factorial function
        String source = "let rec f = fun x y -> if y <= 1 then x else f ( x * y ) ( y - 1 ) in f 1 3";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(result, 6);
    }

    @Test
    public void testMutRec1(){

        // this should calculate the factorial function
        String source = "let rec even = (fun e -> if e == 0 then 1 else odd (e - 1)) and odd = (fun o -> if o == 0 then 0 else 1 - even(o - 1)) in even 77";

        ValueRetriever valueRetriever = new ValueRetriever(source);

        int result = valueRetriever.retrieveValue();

        assertEquals(result, 0);
    }

}
