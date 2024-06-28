public class Token {

    int line;
    TokenType type;
    String lexeme;
    Object value;

    public Token(int line, TokenType type, String lexeme, Object value){
        this.line = line;
        this.type = type;
        this.lexeme = lexeme;
        this.value = value;
    }

    public Token(int line, TokenType type, String lexeme){
        this(line, type, lexeme, null);
    }

    @Override
    public String toString(){
        return String.format("<%s, '%s'>", type.toString(), lexeme);
    }
}
