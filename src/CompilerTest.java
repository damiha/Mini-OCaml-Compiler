import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompilerTest {

    private Compiler compiler;

    @Before
    public void setUpCompiler(){
        compiler = new Compiler();
        compiler.stackDistance = 0;
    }

    @Test
    public void testTupleAccess(){
        Expr expr = new Expr.TupleAccess(new Expr.Tuple(List.of(new Expr.IntLiteral(5), new Expr.IntLiteral(3))), 1);

        Code code = compiler.codeV(expr);

        Code expectedCode = new Code();
        expectedCode.addInstruction(new Instr.LoadC(5), 0);
        expectedCode.addInstruction(new Instr.MakeBasic(), 1);
        expectedCode.addInstruction(new Instr.LoadC(3), 1);
        expectedCode.addInstruction(new Instr.MakeBasic(), 2);
        expectedCode.addInstruction(new Instr.MakeVec(2), 2);
        expectedCode.addInstruction(new Instr.Get(1), 1);

        assertEquals( expectedCode.toString(), code.toString());
    }

    @Test
    public void testTupleUnpacking(){

        /*
        // let (x, y) = (5, 3) in x + y
        Expr expr = new Expr.Let(
                new Expr.Tuple(List.of(new Expr.Variable("x"), new Expr.Variable("y"))),
                new Expr.Tuple(List.of(new Expr.IntLiteral(5), new Expr.IntLiteral(3))),
                new Expr.BinOp(new Expr.Variable("x"), BinaryOperator.PLUS, new Expr.Variable("y"))
        );

        Code code = compiler.codeV(expr);

        Code expectedCode = new Code();

        // 5
        expectedCode.addInstruction(new Instr.LoadC(5), 0);
        expectedCode.addInstruction(new Instr.MakeBasic(), 1);
        // 3
        expectedCode.addInstruction(new Instr.LoadC(3), 1);
        expectedCode.addInstruction(new Instr.MakeBasic(), 2);
        // make into a tuple (consumes arguments and places one on the stack)
        expectedCode.addInstruction(new Instr.MakeVec(2), 2);

        // transforms the address to the vector again into the raw values of the vector
        expectedCode.addInstruction(new Instr.GetVec(2), 1);

        // getvec increases the stack distance by 2
        // x + y

        // load x
        expectedCode.addInstruction(new Instr.PushLoc(2), 3);
        expectedCode.addInstruction(new Instr.GetBasic(), 4);

        // load y
        expectedCode.addInstruction(new Instr.PushLoc(2), 4);
        expectedCode.addInstruction(new Instr.GetBasic(), 5);

        expectedCode.addInstruction(new Instr.Add(), 5);

        // make basic because we have a codeV
        expectedCode.addInstruction(new Instr.MakeBasic(), 4);

        expectedCode.addInstruction(new Instr.Slide(2), 4);

        assertEquals(expectedCode.toString(), code.toString());
         */
    }

    @Test
    public void testMultipleTupleUnpacking(){

        // let (x, y) = (5, 3) in let (a, b) = (1, 2) in a + b + x + y
        Expr expr = new Expr.Let(
                new Expr.Tuple(List.of(new Expr.Variable("x"), new Expr.Variable("y"))),
                new Expr.Tuple(List.of(new Expr.IntLiteral(5), new Expr.IntLiteral(3))),
                new Expr.Let(
                        new Expr.Tuple(List.of(new Expr.Variable("a"), new Expr.Variable("b"))),
                        new Expr.Tuple(List.of(new Expr.IntLiteral(1), new Expr.IntLiteral(2))),
                        new Expr.BinOp(
                                new Expr.Variable("a"),
                                BinaryOperator.PLUS,
                                new Expr.BinOp(new Expr.Variable("b"),
                                        BinaryOperator.PLUS,
                                        new Expr.BinOp(new Expr.Variable("x"),
                                                BinaryOperator.PLUS, new Expr.Variable("y")))
                ))
        );

        Code code = compiler.codeV(expr);

        Code expectedCode = new Code();

        // 5
        expectedCode.addInstruction(new Instr.LoadC(5), 0);
        expectedCode.addInstruction(new Instr.MakeBasic(), 1);
        // 3
        expectedCode.addInstruction(new Instr.LoadC(3), 1);
        expectedCode.addInstruction(new Instr.MakeBasic(), 2);
        // make into a tuple (consumes arguments and places one on the stack)
        expectedCode.addInstruction(new Instr.MakeVec(2), 2);

        // transforms the address to the vector again into the raw values of the vector
        expectedCode.addInstruction(new Instr.GetVec(2), 1);

        // 1
        expectedCode.addInstruction(new Instr.LoadC(1), 2);
        expectedCode.addInstruction(new Instr.MakeBasic(), 3);
        // 2
        expectedCode.addInstruction(new Instr.LoadC(2), 3);
        expectedCode.addInstruction(new Instr.MakeBasic(), 4);
        // make into a tuple (consumes arguments and places one on the stack)
        expectedCode.addInstruction(new Instr.MakeVec(2), 4);

        // transforms the address to the vector again into the raw values of the vector
        expectedCode.addInstruction(new Instr.GetVec(2), 3);

        // load a
        /*
        expectedCode.addInstruction(new Instr.PushLoc(2), 3);
        expectedCode.addInstruction(new Instr.GetBasic(), 4);

        // load b
        expectedCode.addInstruction(new Instr.PushLoc(2), 4);
        expectedCode.addInstruction(new Instr.GetBasic(), 5);

        expectedCode.addInstruction(new Instr.Add(), 5);

        // make basic because we have a codeV
        expectedCode.addInstruction(new Instr.MakeBasic(), 4);

        expectedCode.addInstruction(new Instr.Slide(2), 4);

        assertEquals(expectedCode.toString(), code.toString());
         */
    }

    @Test
    public void testNestedLet(){
        Expr expr = new Expr.Let(
                new Expr.Variable("a"),
                new Expr.IntLiteral(19),
                new Expr.Let(new Expr.Variable("b"),
                        new Expr.BinOp(new Expr.Variable("a"), BinaryOperator.MUL, new Expr.Variable("a")),
                        new Expr.BinOp(new Expr.Variable("a"), BinaryOperator.PLUS, new Expr.Variable("b"))
                )
        );

        Code code = compiler.codeV(expr);

        Code expectedCode = new Code();

        expectedCode.addInstruction(new Instr.LoadC(19), 0);
        expectedCode.addInstruction(new Instr.MakeBasic(), 1);
        expectedCode.addInstruction(new Instr.PushLoc(0), 1);
        expectedCode.addInstruction(new Instr.GetBasic(), 2);
        expectedCode.addInstruction(new Instr.PushLoc(1), 2);
        expectedCode.addInstruction(new Instr.GetBasic(), 3);
        expectedCode.addInstruction(new Instr.Mul(), 3);
        expectedCode.addInstruction(new Instr.MakeBasic(), 2);
        expectedCode.addInstruction(new Instr.PushLoc(1), 2);
        expectedCode.addInstruction(new Instr.GetBasic(), 3);
        expectedCode.addInstruction(new Instr.PushLoc(1), 3);
        expectedCode.addInstruction(new Instr.GetBasic(), 4);
        expectedCode.addInstruction(new Instr.Add(), 4);
        expectedCode.addInstruction(new Instr.MakeBasic(), 3);
        expectedCode.addInstruction(new Instr.Slide(2), 3);

        assertEquals(expectedCode.toString(), code.toString());
    }

    @Test
    public void testFunctionDefinition(){
        Expr expr = new Expr.Let(
                new Expr.Variable("a"),
                new Expr.IntLiteral(1),
                new Expr.FunctionDefinition(
                        List.of(new Expr.Variable("b")),
                        new Expr.BinOp(new Expr.Variable("a"), BinaryOperator.PLUS, new Expr.Variable("b"))
                )
        );

        Code code = compiler.codeV(expr);

        Code expectedCode = new Code();

        expectedCode.addInstruction(new Instr.LoadC(1), 0);
        expectedCode.addInstruction(new Instr.MakeBasic(), 1);
        expectedCode.addInstruction(new Instr.PushLoc(0), 1);
        expectedCode.addInstruction(new Instr.MakeVec(1), 2);
        expectedCode.addInstruction(new Instr.MakeFunVal("_0"), 2);
        expectedCode.addInstruction(new Instr.Jump("_1"), 2);
        expectedCode.addInstruction(new Instr.TestArg(1), "_0", 0);
        expectedCode.addInstruction(new Instr.PushGlob(0), 0);
        expectedCode.addInstruction(new Instr.GetBasic(), 1);
        expectedCode.addInstruction(new Instr.PushLoc(1), 1);
        expectedCode.addInstruction(new Instr.GetBasic(), 2);
        expectedCode.addInstruction(new Instr.Add(), 2);
        expectedCode.addInstruction(new Instr.MakeBasic(), 1);
        expectedCode.addInstruction(new Instr.Return(1), 1);
        expectedCode.addInstruction(new Instr.Slide(1), "_1", 2);

        assertEquals(expectedCode.toString(), code.toString());
    }

    @Test
    public void testFunctionApplication1(){
        Expr expr = new Expr.Let(
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

        Code code = compiler.codeV(expr);

        Code expectedCode = new Code();

        expectedCode.addInstruction(new Instr.LoadC(17), 0);
        expectedCode.addInstruction(new Instr.MakeBasic(), 1);
        expectedCode.addInstruction(new Instr.PushLoc(0), 1);
        expectedCode.addInstruction(new Instr.MakeVec(1), 2);
        expectedCode.addInstruction(new Instr.MakeFunVal("_0"), 2);
        expectedCode.addInstruction(new Instr.Jump("_1"), 2);
        expectedCode.addInstruction(new Instr.TestArg(1), "_0", 0);
        expectedCode.addInstruction(new Instr.PushGlob(0), 0);
        expectedCode.addInstruction(new Instr.GetBasic(), 1);
        expectedCode.addInstruction(new Instr.PushLoc(1), 1);
        expectedCode.addInstruction(new Instr.GetBasic(), 2);
        expectedCode.addInstruction(new Instr.Add(), 2);
        expectedCode.addInstruction(new Instr.MakeBasic(), 1);
        expectedCode.addInstruction(new Instr.Return(1), 1);
        expectedCode.addInstruction(new Instr.Mark("_2"), "_1", 2);
        expectedCode.addInstruction(new Instr.LoadC(42), 5);
        expectedCode.addInstruction(new Instr.MakeBasic(), 6);
        expectedCode.addInstruction(new Instr.PushLoc(4), 6);
        expectedCode.addInstruction(new Instr.Apply(), 7);
        expectedCode.addInstruction(new Instr.Slide(2), "_2", 3);

        assertEquals(expectedCode.toString(), code.toString());
    }

    @Test
    public void testFunctionApplication2(){
        // TODO
    }

    @Test
    public void testLetRec1(){
        Expr expr = new Expr.LetRec(
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

        Code code = compiler.codeV(expr);

        Code expected = new Code();

        expected.addInstruction(new Instr.Alloc(1), 0);
        expected.addInstruction(new Instr.PushLoc(0), 1);
        expected.addInstruction(new Instr.MakeVec(1), 2);
        expected.addInstruction(new Instr.MakeFunVal("_0"), 2);
        expected.addInstruction(new Instr.Jump("_1"), 2);
        expected.addInstruction(new Instr.TestArg(2), "_0", 0);
        // translate the if else
        expected.addInstruction(new Instr.PushLoc(1),  0);
        expected.addInstruction(new Instr.GetBasic(), 1);
        expected.addInstruction(new Instr.LoadC(1), 1);
        expected.addInstruction(new Instr.LessThanOrEqual(), 2);
        expected.addInstruction(new Instr.JumpZ("_2"), 1);
        expected.addInstruction(new Instr.PushLoc(1), 1);
        // jump over else
        expected.addInstruction(new Instr.Jump("_3"), 2);

        // else branch
        expected.addInstruction(new Instr.Mark("_4"), "_2", 2);

        // y - 1
        expected.addInstruction(new Instr.PushLoc(6), 5);
        expected.addInstruction(new Instr.GetBasic(), 6);
        expected.addInstruction(new Instr.LoadC(1), 6);
        expected.addInstruction(new Instr.Sub(), 7);
        expected.addInstruction(new Instr.MakeBasic(), 6);

        // x * y
        expected.addInstruction(new Instr.PushLoc(6), 6);
        expected.addInstruction(new Instr.GetBasic(), 7);
        expected.addInstruction(new Instr.PushLoc(8), 7);
        expected.addInstruction(new Instr.GetBasic(), 8);
        expected.addInstruction(new Instr.Mul(), 8);
        expected.addInstruction(new Instr.MakeBasic(), 7);

        // MakeVec only for function definition, not for function application

        // put the function name on the stack
        // f is defined outside (because it is used in the function definition and is not a parameter)
        expected.addInstruction(new Instr.PushGlob(0), 7);

        expected.addInstruction(new Instr.Apply(), 8);

        expected.setJumpLabelAtEnd("_4");
        expected.addInstruction(new Instr.Return(2), "_3", 3);

        expected.addInstruction(new Instr.Rewrite(1),"_1", 2);

        // now the in f 1
        // to generate this, we use stack distance before let rec + how many defined
        //  sd = 0 + 1 defined (= f) so 1
        expected.addInstruction(new Instr.Mark("_5"), 1);

        expected.addInstruction(new Instr.LoadC(1), 4);
        expected.addInstruction(new Instr.MakeBasic(), 5);

        expected.addInstruction(new Instr.PushLoc(4), 5);
        expected.addInstruction(new Instr.Apply(), 6);


        // sd before the function call + 1
        expected.addInstruction(new Instr.Slide(1), "_5", 2);

        assertEquals(expected.toString(), code.toString());
    }
}