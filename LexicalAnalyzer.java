/*
Class:       CS 4308 Section 03
Term:        Fall 2021
Name:       Faith Swetnam
Instructor:   Sharon Perry
Project:     Deliverable 1 Scanner
Updated: 10/28/2021
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class LexicalAnalyzer {

    //Global static variables
    static char nextChar;                                                   //char to store the next character from file
    static char[] sourceArray = new char[100]; 	                            //stores all characters from the source file
    static ArrayList<Character> lexeme = new ArrayList<Character>();	  	//stores lexemes
    static int sourceCount = 0;					                            //stores position in sourceArray array
    static Token.CharacterClass currCharClass;                              //CharacterClass of nextChar
    static Token.CharacterClass prevCharClass;                              //CharacterClass for previous nextChar
    static ArrayList<Token> tokens = new ArrayList<Token>();                //stores all tokens produced
    static int sourceLine = 0;                                              //stores the line of source the lexeme is on
    static ArrayList<Error> errors = new ArrayList<Error>();                //stores errors that are found
    static boolean errorOccurred = false;                                   //stores whether an error has occurred
    static String validSymbols = "=<>+-/*_()!";                             //stores valid symbols for Julia
    static int tokenCount = 0;

    //ReadFile reads File f and returns a char[] of contents of the file
    //Adds a '\n' at the end of each line and '\u001a' at the end of the file for processing reasons
    static char[] readFile(File f) {
        String source = "";
        try {
            Scanner fileReader = new Scanner(f);
            while(fileReader.hasNextLine()) {
                source += fileReader.nextLine() + '\n';
            }
            source += '\u001a';
            sourceArray = source.toCharArray();
            fileReader.close();
            return sourceArray;
        } catch (FileNotFoundException e) {
            Error err = new Error("File could not be found");
            errors.add(err);
            errorOccurred = true;
            return sourceArray;
        }
    }

    //GetChar returns the next character from sourceArray
    //It sets the currCharClass based on value of nextChar
    static char getChar() {
        nextChar = sourceArray[sourceCount];
        if(nextChar != '\u001a') {
            if(Character.isLetter(nextChar)) {
                currCharClass = Token.CharacterClass.LETTER;
            } else if (Character.isDigit(nextChar)) {
                currCharClass = Token.CharacterClass.DIGIT;
            } else {
                currCharClass = Token.CharacterClass.UNKNOWN;
            }
            sourceCount++;
        } else {
            currCharClass = Token.CharacterClass.EOF;
        }
        return nextChar;
    }

    //AddChar adds the value in nextChar to lexeme
    //if lexeme is too long it logs an error
    static void addChar() {
        if(lexeme.size() < 100) {
            lexeme.add(nextChar);
        } else {
            Error err = new Error("value is too long", sourceLine);
            errors.add(err);
            errorOccurred = true;
            addToken(Token.TokenType.ERROR, "value is too long", sourceLine);
        }
    }

    //Function to process (skip over) white-spaces, tabs, new lines in the file
    //Logs an error if program ends unexpectedly
    static void processFormatting() {
        while (Character.isSpaceChar(nextChar) || nextChar == '\t' || nextChar == '\n') {
            if(nextChar == '\n'){
                sourceLine++;
            }
            if(nextChar != '\u001a') {
                getChar();
            } else {
                Error err = new Error("program ended unexpectedly", sourceLine);
                errors.add(err);
                errorOccurred = true;
                addToken(Token.TokenType.ERROR, "program ended unexpectedly", sourceLine);
            }
        }

    }

    //Function to process single line comments
    //Logs an error if program ends during comment
    static void comments() {
        if(nextChar == '/' && sourceArray[sourceCount] =='/') {
            processFormatting();
            while(nextChar != '\n') {
                if(nextChar != '\u001a') {
                    getChar();
                    char tempChar = nextChar;
                    getChar();
                    if(nextChar == '/'){
                        comments();
                        sourceLine++;
                    } else {
                        nextChar = tempChar;
                        sourceCount--;
                    }
                } else {
                    Error err = new Error("program ended unexpectedly", sourceLine);
                    errors.add(err);
                    errorOccurred = true;
                    addToken(Token.TokenType.ERROR, "program ended unexpectedly", sourceLine);
                }
            }
        }
    }

    //String turns the lexeme into a string for processing purposes
    //It returns the string lex when finished
    static String string() {
        String lex = "";

        if(lexeme.size() > 1) {
            StringBuilder builder = new StringBuilder(lexeme.size());
            for(Character c: lexeme) {
                if(c.charValue() != '\u001a')
                    builder.append(c);
                else {
                    Error err = new Error("program ended unexpectedly", sourceLine);
                    errors.add(err);
                    errorOccurred = true;
                    addToken(Token.TokenType.ERROR, "program ended unexpectedly", sourceLine);
                    break;
                }
            }
            lex = builder.toString();
        } else if (lexeme.size() > 0){
            if(lexeme.get(0) != '\u001a')
                lex = Character.toString(lexeme.get(0));
            else {
                Error err = new Error("program ended unexpectedly", sourceLine);
                errors.add(err);
                errorOccurred = true;
                addToken(Token.TokenType.ERROR, "program ended unexpectedly", sourceLine);
            }
        } else {
            Error err = new Error("lexeme array is empty", sourceLine);
            errors.add(err);
            errorOccurred = true;
            addToken(Token.TokenType.ERROR, "lexeme array is empty", sourceLine);
        }

        return lex;
    }

    //adds token to token arraylist
    static void addToken(Token.TokenType tokenType, String lex, int sourceLine){
        Token currToken = new Token(tokenType, lex, sourceLine);
        tokens.add(currToken);
    }

    //LookUp determines the token type based on the lexeme
    static void lookUp(String lex) {
        Token.TokenType tokenType;
        int line = sourceLine + 1;
        //if the lexeme is a word(contains alphabetical letters) do
        if(prevCharClass == Token.CharacterClass.LETTER) {
            switch (lex) {
                case "end":
                    if(sourceCount == sourceArray.length-1) {
                        tokenType = Token.TokenType.EOF;
                    } else {
                        tokenType = Token.TokenType.END;
                    }
                    addToken(tokenType, lex, line);
                    break;
                case "function":
                    tokenType = Token.TokenType.FUNCT;
                    addToken(tokenType, lex, line);
                    break;
                case "while":
                    tokenType = Token.TokenType.WHILE;
                    addToken(tokenType, lex, line);
                    break;
                case "do":
                    tokenType = Token.TokenType.DO;
                    addToken(tokenType, lex, line);
                    break;
                case "print":
                    tokenType = Token.TokenType.PRINT;
                    addToken(tokenType, lex, line);
                    break;
                case "if":
                    tokenType = Token.TokenType.IF;
                    addToken(tokenType, lex, line);
                    break;
                case "then":
                    tokenType = Token.TokenType.THEN;
                    addToken(tokenType, lex, line);
                    break;
                case "else":
                    tokenType = Token.TokenType.ELSE;
                    addToken(tokenType, lex, line);
                    break;
                case "repeat":
                    tokenType = Token.TokenType.REPEAT;
                    addToken(tokenType, lex, line);
                    break;
                case "until":
                    tokenType = Token.TokenType.UNTIL;
                    addToken(tokenType, lex, line);
                    break;
                case "null":
                    tokenType = Token.TokenType.NULL;
                    addToken(tokenType, lex, line);
                    break;
                case "error":
                    tokenType = Token.TokenType.ERROR;
                    addToken(tokenType, lex, line);
                    break;
                default:  //lexeme is an identifier
                    tokenType = Token.TokenType.LETTER;
                    addToken(tokenType, lex, line);
                    break;
            }
            //else lexeme is a symbol or number
        } else {
            switch(lex) {
                case "=":
                    tokenType = Token.TokenType.ASSIGN_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "<=":
                    tokenType = Token.TokenType.LE_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "<":
                    tokenType = Token.TokenType.LT_OP;
                    addToken(tokenType, lex, line);
                    break;
                case ">=":
                    tokenType = Token.TokenType.GE_OP;
                    addToken(tokenType, lex, line);
                    break;
                case ">":
                    tokenType = Token.TokenType.GT_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "==":
                    tokenType = Token.TokenType.EQ_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "!=":
                    tokenType = Token.TokenType.NE_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "+=":
                    tokenType = Token.TokenType.AE_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "+":
                    tokenType = Token.TokenType.ADD_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "-":
                    tokenType = Token.TokenType.SUB_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "*":
                    tokenType = Token.TokenType.MUL_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "/":
                    tokenType = Token.TokenType.DIV_OP;
                    addToken(tokenType, lex, line);
                    break;
                case "(":
                    tokenType = Token.TokenType.L_PAREN;
                    addToken(tokenType, lex, line);
                    break;
                case ")":
                    tokenType = Token.TokenType.R_PAREN;
                    addToken(tokenType, lex, line);
                    break;
                default:
                    tokenType = Token.TokenType.DIGIT;
                    addToken(tokenType, lex, line);
                    break;
            }
        }
    }

    //checkValid determines if the unknown symbol is valid in the language
    static void checkValid(){
        String check = Character.toString(nextChar);
        if(!Character.isLetterOrDigit(nextChar)) {
            if (!validSymbols.contains(check)) {
                Error err = new Error("unexpected symbol", check, sourceLine);
                errors.add(err);
                errorOccurred = true;
                addToken(Token.TokenType.ERROR, "unexpected symbol", sourceLine);
            }
        }
    }

    //main body of the lexical_analyzer
    //returns a token based on the lexeme found
    public static Token lexer() {
        lexeme = new ArrayList<Character>();
        comments();
        processFormatting();
        switch (currCharClass) {
            case LETTER:
                addChar();
                getChar();
                while((currCharClass == Token.CharacterClass.LETTER || nextChar == '_') && !errorOccurred) {
                    addChar();
                    getChar();
                }
                prevCharClass = Token.CharacterClass.LETTER;
                break;
            case DIGIT:
                addChar();
                getChar();
                while((currCharClass == Token.CharacterClass.DIGIT || nextChar == '.') && !errorOccurred) {
                    addChar();
                    getChar();
                }
                prevCharClass = Token.CharacterClass.DIGIT;
                break;
            case UNKNOWN:
                comments();
                processFormatting();
                checkValid();
                addChar();
                getChar();
                while(validSymbols.contains(Character.toString(nextChar)) && nextChar != '(' && nextChar != ')' && !errorOccurred){
                    addChar();
                    getChar();
                    if(nextChar == '(' || nextChar == ')'){
                        break;
                    }
                }
                prevCharClass = Token.CharacterClass.UNKNOWN;
                break;
            default:
                prevCharClass = Token.CharacterClass.EOF;
                break;
        }
        if (prevCharClass != Token.CharacterClass.EOF && !errorOccurred) {
            String lex = string();
            lookUp(lex);
            tokenCount++;
            return tokens.get(tokenCount-1);
        } else if(errorOccurred){
            return new Token(Token.TokenType.ERROR, sourceLine);
        } else
            return new Token(Token.TokenType.EOF, "end", sourceLine);
    }

    //checks to ensure that the program ended correctly with 'end'
    //returns a boolean
    static boolean endedCorrectly(){
        if(tokens.size() == 0){
            Error err = new Error("no tokens initialized", sourceLine);
            errors.add(err);
            errorOccurred = true;
            addToken(Token.TokenType.ERROR, "no tokens initialized", sourceLine);
        } else if(tokens.get(tokens.size()-1).getType() != Token.TokenType.EOF){
            Error err = new Error("program ended unexpectedly", sourceLine);
            errors.add(err);
            errorOccurred = true;
            addToken(Token.TokenType.ERROR, "program ended unexpectedly", sourceLine);
        }
        return errorOccurred;
    }

    //printTokenTable takes in an ArrayList of Token objects and prints out a symbol table
    static void printTokenTable(ArrayList<Token> tokens) {
        System.out.printf("%-10s\t%-20s\t%-10s\t%-10s\n", "Lexeme", "Token Type", "Opcode", "Line");
        System.out.println("-----------------------------------------------------\n");
        for(Token t: tokens) {
            t.printToken();
        }
    }

    //printErrorTable takes in an ArrayList of Error objects and prints them out in a table
    static void printErrorTable(ArrayList<Error> errors) {
        System.out.printf("%-50s\t%-10s\t%-10s\n", "Errors", "Value", "Line Occured");
        System.out.println("-----------------------------------------------------------------------------\n");
        for(Error e: errors) {
            e.printError();
        }
    }

    //ADDITION
    //Function sets up and runs the lexical analyzer without the main and returns the ArrayList of Tokens produced
    static ArrayList<Token> getTokenList(File f){
        readFile(f);
        getChar();
        while(currCharClass != Token.CharacterClass.EOF && !errorOccurred) {
            lexer();
        }

        endedCorrectly();

        if(errorOccurred) {
            System.out.println("Lexical Analyzer Errors:");
            printErrorTable(errors);
            return null;
        } else
            return tokens;
    }


    public static void main(String args[]) {
        File f = new File("src/Julia-Files/Test2.jl");
        readFile(f);
        getChar();
        while(currCharClass != Token.CharacterClass.EOF && !errorOccurred) {
            lexer();
        }

        endedCorrectly();

        if(errorOccurred)
            printErrorTable(errors);
        else
            printTokenTable(tokens);

    }

}