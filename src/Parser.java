import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Parser {

    List<Token> tokens;

    int current = 0;

    public Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private Token previous(){
        return tokens.get(current - 1);
    }

    private boolean isAtEnd(){
        return current >= tokens.size() || peek().type == TokenType.EOF;
    }

    private boolean check(TokenType type){
        return !isAtEnd() && peek().type == type;
    }

    // check = checkAtK with k = 0
    // realizes a lookahead
    private boolean checkAtK(TokenType type, int k){
        return (current + k < tokens.size()) && ((tokens.get(current + k)).type == type);
    }

    private boolean checkSequence( int start, TokenType... types){
        for(int i = 0; i < types.length; i++){
            if(!checkAtK(types[i], start + i)){
                return false;
            }
        }
        return true;
    }

    private boolean checkTypes(TokenType... types){
        for(TokenType t : types){
            if(check(t)){
                return true;
            }
        }
        return false;
    }

    private void advance(){
        current++;
    }

    private boolean match(TokenType type){

        if(check(type)){
            advance();
            return true;
        }

        return false;
    }

    private Token consume(TokenType type, String message){
        if(match(type)){
            return previous();
        }
        error(message);
        return null;
    }

    public Expr parse(){
        return expression();
    }

    private void error(String message){
        throw new RuntimeException(String.format("[%d] %s", peek().line, message));
    }

    private Expr expression(){
        return let();
    }

    private Expr let(){

        if(match(TokenType.LET)){
            if(match(TokenType.REC)){
                return letRec();
            }
            return simpleLet();
        }
        else{
            return ifExpr();
        }
    }

    // let already processed
    private Expr simpleLet(){

        Expr target = primary();

        consume(TokenType.EQUAL, "let expects '='");

        // 'let a = let b in b in a' must be possible
        Expr rightHandSide = expression();

        consume(TokenType.IN, "let expects 'in'");

        Expr inExpr = expression();

        return new Expr.Let(target, rightHandSide, inExpr);
    }

    // let and rec already processed
    private Expr letRec(){

        List<Pair<Expr, Expr>> parallelDefs = new ArrayList<>();

        do{

            // target is super restricted?
            Expr target = primary();
            consume(TokenType.EQUAL, "let rec expects '='");

            Expr rightHandSide = expression();

            parallelDefs.add(new Pair<>(target, rightHandSide));

        }while(match(TokenType.AND));

        consume(TokenType.IN, "let rect expects 'in'");

        // body can be anything again (another let for example)
        Expr inExpr = expression();

        return new Expr.LetRec(parallelDefs, inExpr);
    }

    private Expr ifExpr(){

        if(match(TokenType.IF)){

            Expr condition = expression();

            consume(TokenType.THEN, "if expects 'then'");

            Expr ifBranchExpr = expression();

            consume(TokenType.ELSE, "if ... then expects 'else");

            Expr elseBranchExpr = expression();

            return new Expr.If(condition, ifBranchExpr, elseBranchExpr);
        }

        return functionDefinition();
    }

    private Expr functionDefinition(){
        if(match(TokenType.FUN)){

            List<Expr> variables = new ArrayList<>();

            // functions have at least one argument!
            do{
                // variables are restricted
                variables.add(primary());

            }while(!match(TokenType.ARROW));

            // function body can be anything again (another let for example)
            Expr functionBody = expression();

            return new Expr.FunctionDefinition(variables, functionBody);
        }

        return logic_or();
    }

    private Expr logic_or(){

        Expr expr = logic_and();

        while(match(TokenType.DOUBLE_PIPE)){
            Expr rightHandSide = logic_and();
            expr = new Expr.BinOp(expr, BinaryOperator.OR, rightHandSide);
        }

        return expr;
    }

    private Expr logic_and(){
        Expr expr = equality();

        while(match(TokenType.DOUBLE_AMPERSAND)){
            Expr rightHandSide = equality();
            expr = new Expr.BinOp(expr, BinaryOperator.AND, rightHandSide);
        }

        return expr;
    }

    private Expr equality(){
        Expr expr = comparison();

        while(checkTypes(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)){

            BinaryOperator operator = null;
            if(match(TokenType.EQUAL_EQUAL)) {
                operator = BinaryOperator.EQUAL;
            }
            // second match is important to advance the current pointer?
            else if(match(TokenType.BANG_EQUAL)){
                operator = BinaryOperator.UNEQUAL;
            }
            Expr rightHandSide = comparison();
            expr = new Expr.BinOp(expr, operator, rightHandSide);
        }

        return expr;
    }

    // done
    private Expr comparison(){
        Expr expr = term();

        while(checkTypes(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)){

            BinaryOperator operator = null;
            if(match(TokenType.LESS)) {
                operator = BinaryOperator.LESS;
            }
            else if(match(TokenType.LESS_EQUAL)){
                operator = BinaryOperator.LEQ;
            }
            else if(match(TokenType.GREATER_EQUAL)){
                operator = BinaryOperator.GEQ;
            }
            else if(match(TokenType.GREATER)){
                operator = BinaryOperator.GREATER;
            }
            Expr rightHandSide = term();
            expr = new Expr.BinOp(expr, operator, rightHandSide);
        }

        return expr;
    }

    // done
    private Expr term(){
        Expr expr = factor();

        while(checkTypes(TokenType.PLUS, TokenType.MINUS)){

            BinaryOperator operator = null;
            if(match(TokenType.PLUS)) {
                operator = BinaryOperator.PLUS;
            }
            // second match is important to advance the current pointer?
            else if(match(TokenType.MINUS)){
                operator = BinaryOperator.MINUS;
            }
            Expr rightHandSide = factor();
            expr = new Expr.BinOp(expr, operator, rightHandSide);
        }

        return expr;
    }

    private Expr factor(){

        Expr expr = unary();

        while(checkTypes(TokenType.STAR, TokenType.SLASH)){

            BinaryOperator operator = null;
            if(match(TokenType.STAR)) {
                operator = BinaryOperator.MUL;
            }
            else if(match(TokenType.SLASH)){
                operator = BinaryOperator.DIV;
            }
            else if(match(TokenType.PERCENT)){
                operator = BinaryOperator.MOD;
            }
            Expr rightHandSide = unary();
            expr = new Expr.BinOp(expr, operator, rightHandSide);
        }

        return expr;
    }

    private Expr unary(){

        if(match(TokenType.BANG)){
            return new Expr.UnOp(UnaryOperator.NOT, unary());
        }
        else if(match(TokenType.MINUS)){
            return new Expr.UnOp(UnaryOperator.MINUS, unary());
        }
        return call();
    }

    private Expr call(){

        Expr expr = primary();

        Expr followUp;

        List<Expr> arguments = new ArrayList<>();

        while((followUp = primary()) != null){
            arguments.add(followUp);
        }

        // functions must have at least one argument so we now it's not a function
        if(arguments.isEmpty()){
            return expr;
        }
        else{
            return new Expr.FunctionApplication(expr, arguments);
        }
    }

    private Expr primary(){

        if(match(TokenType.NUMBER)){
            return new Expr.IntLiteral((Integer)previous().value);
        }
        else if(match(TokenType.IDENTIFIER)){
            return new Expr.Variable(previous().lexeme);
        }
        else if(match(TokenType.LEFT_PAREN)){

            List<Expr> elements = new ArrayList<>();

            Expr exprToReturn;

            do{
                // let (a, b) = (5 + 3, 10) in a + b must be possible
                elements.add(expression());
            }while(match(TokenType.COMMA));

            if(elements.size() == 1){
                exprToReturn = elements.getFirst();
            }
            else{
                exprToReturn = new Expr.Tuple(elements);
            }

            consume(TokenType.RIGHT_PAREN, "Opening ( needs to be closed.");
            return exprToReturn;
        }

        // when primary can't be parsed?
        return null;
    }
}
