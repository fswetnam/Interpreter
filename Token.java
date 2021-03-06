/*
Class:       CS 4308 Section 03
Term:        Fall 2021
Name:       Faith Swetnam
Instructor:   Sharon Perry
Project:     Deliverable 1 Scanner
Updated: 10/28/2021
 */

//Token object holds a tokens TokenType type, lexeme (value), and the line its on
public class Token {
    private TokenType type;
    private String lexeme;
    private int line;

    //Token constructor
    Token(TokenType type, String lexeme, int line){
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
    }

    Token(TokenType type, int line){
        this.type = type;
        this.lexeme = "ERROR";
        this.line = line;
    }

    //Added
    Token(){
        this.type = TokenType.NULL;
        this.lexeme = "NULL";
        this.line = 0;
    }

    //Prints out token values
    void printToken() {
        System.out.printf("%-10s\t%-20s\t%-10d\t%-10d\n", lexeme, type.label, type.opcode, line);
    }

    //Returns lexeme
    String getLexeme(){ return lexeme; }

    //Returns line
    int getLine(){ return line; }

    //Returns TokenType
    TokenType getType(){
        return type;
    }
    //Returns if the tokens TokenType is equal to the passed TokenType
    boolean checkType(TokenType tokenType){
        if(type == tokenType){
            return true;
        }
        return false;
    }

    //Enumerated type to hold the type of character in nextChar
    //Each CharacterClass has a description for printing
    enum CharacterClass {
        LETTER ("identifier"),
        DIGIT ("number"),
        UNKNOWN ("unknown symbol"),
        EOF("end of program");

        final String description;

        private CharacterClass(String description) {
            this.description = description;
        }
    }

    //Enumerated type to hold legal keywords and symbols (essentially the legal token types allowed by language)
    //Each TokenType has an associated label and opcode. It should contain the String value that holds the lexeme
    enum TokenType {
        //ADDITIONS: ERROR, NULL, END, REPEAT
        NULL("null", "", 0),
        LETTER("identifier", "", 1),
        DIGIT("number", "", 2),
        EOF("end of file", "end", 99),
        END("keyword_end", "end", 1000),
        FUNCT("keyword_function", "function", 1001),
        WHILE("keyword_while", "while", 1002),
        DO("keyword_do", "do", 1003),
        PRINT("keyword_print", "print", 1004),
        IF("keyword_if", "if", 1005),
        THEN("keyword_then", "then", 1006),
        ELSE("keyword_else", "else", 1007),
        REPEAT("keyword_repeat", "repeat", 1008),
        UNTIL("keyword_until", "until", 1009),
        ASSIGN_OP("assignment_operator", "=", 2000),
        LE_OP("less_equal", "<=", 2001),
        LT_OP("less", "<", 2002),
        GE_OP("greater_equal", ">=", 2003),
        GT_OP("greater", ">", 2004),
        EQ_OP("equal", "==", 2005),
        NE_OP("not_equal", "!=", 2006),
        AE_OP("addition_assignment", "+=", 2007),
        ADD_OP("addition_operator", "+", 2008),
        SUB_OP("subtraction_operator", "-", 2009),
        MUL_OP("multiplication_operator", "*", 2010),
        DIV_OP("division_operator", "/", 2011),
        L_PAREN("left_parenthesis", "(", 2012),
        R_PAREN("right_parenthesis", ")", 2013),
        ERROR("error", new Error("error occurred"), 9999);

        final String label;
        final int opcode;
        Error e =  new Error("error occurred");
        String value;

        //TokenType constructor
        private TokenType(String label, String value, int opcode) {
            this.label = label;
            this.value = value;
            this.opcode = opcode;
        }

        private TokenType(String label, Error e, int opcode){
            this.label = label;
            this.e = e;
            this.opcode = opcode;
        }
    }
}
