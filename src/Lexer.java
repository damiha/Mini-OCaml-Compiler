import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Lexer {

    String source;
    int current = 0;
    int start = 0;
    int line = 0;

    List<Token> tokens;

    Map<String, TokenType> keywordToToken;

    public Lexer(String source){
        this.source = source;
        tokens = new ArrayList<>();

        keywordToToken = new HashMap<>();

        keywordToToken.put("if", TokenType.IF);
        keywordToToken.put("then", TokenType.THEN);
        keywordToToken.put("else", TokenType.ELSE);
        keywordToToken.put("let", TokenType.LET);
        keywordToToken.put("in", TokenType.IN);
        keywordToToken.put("rec", TokenType.REC);
        keywordToToken.put("fun", TokenType.FUN);
    }

    public List<Token> getTokens(){

        while(!isAtEnd()){

            char c = advance();

            switch(c){
                case ' ':
                    break;
                case '\n':
                    line++;
                    break;
                case '/':

                    if(match('/')){
                        comment();
                    }
                    else {
                        addToken(TokenType.SLASH);
                    }
                    break;
                case '(':
                    addToken(TokenType.LEFT_PAREN);
                    break;
                case ')':
                    addToken(TokenType.RIGHT_PAREN);
                    break;
                case '[':
                    addToken(TokenType.LEFT_BRACKET);
                    break;
                case ']':
                    addToken(TokenType.RIGHT_BRACKET);
                    break;
                case '*':
                    addToken(TokenType.STAR);
                    break;
                case '+':
                    addToken(TokenType.PLUS);
                    break;
                case '<':

                    if(match('=')){
                        addToken(TokenType.LESS_EQUAL);
                    }
                    else{
                        addToken(TokenType.LESS);
                    }
                    break;
                case '-':
                    if(match('>')){
                        addToken(TokenType.ARROW);
                    }
                    else {
                        addToken(TokenType.MINUS);
                    }
                    break;
                case ',':
                    addToken(TokenType.COMMA);
                    break;
                case '!':
                    if(match('=')){
                        addToken(TokenType.BANG_EQUAL);
                    }
                    else{
                        addToken(TokenType.BANG);
                    }
                    break;
                case '=':
                    if(match('=')){
                        addToken(TokenType.EQUAL_EQUAL);
                    }
                    else{
                        addToken(TokenType.EQUAL);
                    }
                    break;
                case '>':
                    if(match('=')){
                        addToken(TokenType.GREATER_EQUAL);
                    }
                    else{
                        addToken(TokenType.GREATER);
                    }
                    break;
                case '&':
                    if(match('&')){
                        addToken(TokenType.DOUBLE_AMPERSAND);
                    }
                    else{
                        throw new RuntimeException("Single & is not defined");
                    }
                    break;
                case '|':
                    if(match('|')){
                        addToken(TokenType.DOUBLE_PIPE);
                    }
                    else{
                        throw new RuntimeException("Single | is not defined");
                    }
                    break;
            }

            // for identifies (can be keywords)
            if(isAlpha(c)){

                // either keyword or variable
                identifier();
            }
            else if(isNumeric(c)){
                number();
            }
        }

        tokens.add(new Token(line, TokenType.EOF, "", null));

        return tokens;
    }

    private void number(){
        while(isNumeric(peek())){
            advance();
        }

        addToken(TokenType.NUMBER);
    }

    private void identifier(){
        while(isAlphaNumeric(peek())){
            advance();
        }

        addToken(keywordToToken.getOrDefault(getLexeme(), TokenType.IDENTIFIER));
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isNumeric(c);
    }

    private boolean isNumeric(char c){
        return '0' <= c && c <= '9';
    }

    private void comment(){
        while(!isAtEnd() && peek() != '\n'){
            advance();
        }
        start = current;
    }

    private boolean isAlpha(char c){
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    private String getLexeme(){
        return source.substring(start, current).strip();
    }

    private void addToken(TokenType type){

        String lexeme = getLexeme();

        Object value = null;

        if(type == TokenType.NUMBER){
            value = Integer.parseInt(lexeme);
        }

        tokens.add(new Token(line, type, lexeme, value));

        // we successfully added a token
        start = current;
    }

    private char advance(){
        return source.charAt(current++);
    }

    private char peek(){
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private boolean match(char c){
        if(peek() == c){
            advance();
            return true;
        }
        return false;
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }
}

