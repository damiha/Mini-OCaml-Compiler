public enum TokenType {
    LEFT_PAREN, RIGHT_PAREN,
    COMMA, MINUS, PLUS, SLASH, STAR,
    LEFT_BRACKET, RIGHT_BRACKET,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    ARROW,
    DOUBLE_AMPERSAND, DOUBLE_PIPE,

    // Literals.
    IDENTIFIER, NUMBER,

    // Keywords.
    IF, THEN, ELSE,
    LET, IN, REC, FUN,

    EOF
}
