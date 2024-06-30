public enum TokenType {
    LEFT_PAREN, RIGHT_PAREN,
    COMMA, MINUS, PLUS, SLASH, STAR,
    LEFT_BRACKET, RIGHT_BRACKET,
    HASH,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    ARROW,
    DOUBLE_AMPERSAND, DOUBLE_PIPE,
    PERCENT,
    DOUBLE_COLON,

    // Literals.
    IDENTIFIER, NUMBER,

    // Keywords.
    IF, THEN, ELSE,
    LET, IN, REC, FUN, AND,  // used for let rec y_1 = e_1 and ...

    EOF
}
