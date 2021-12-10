/*
Class:       CS 4308 Section 03
Term:        Fall 2021
Name:       Faith Swetnam
Instructor:   Sharon Perry
Project:     Deliverable 3 Interpreter
 */

import java.io.File;
import java.util.ArrayList;

public class Interpreter {

    //create an object to store a variable's name and value
    static class Variable {
        private String name = "";
        private String value = "";

        Variable(String name, String value){ this.name = name; this.value = value; }
        void setID (String name){ this.name = name; }
        String getID() { return name; }
        String getValue() { return value; }
    }

    private static Node root; //root node of the AST
    private static ArrayList<Node> nodes; //an ArrayList of all nodes in the AST
    private static ArrayList<Token> tokens; //an ArrayList to hold all Tokens
    private static ArrayList<Error> errors = new ArrayList<Error>(); //an ArrayList to hold all errors generated
    private static ArrayList<Variable> vars = new ArrayList<Variable>(); //an ArrayList to hold all Variables initialized
    private static boolean errorOccurred = false; //boolean to determine whether error has occurred
    private static String output = ""; //holds output of file

    //generates an error object, adds it to errors arraylist, and sets errorOccurred to true
    //takes in an error message, the value that threw the error, and the line it occurred on
    private static void createError(String msg){
        Error e = new Error(msg);
        errors.add(e);
        errorOccurred = true;
    }

    private static void createError(String msg, String value, int line){
        Error e = new Error(msg, value, line);
        errors.add(e);
        errorOccurred = true;
    }

    //checks whether a variable has already been initialized
    //returns the Variable if found, returns null if not
    private static Variable varsContains(String id){
        for(Variable var: vars){
            if(var.getID().equals(id)){
                return var;
            }
        }
        return null;
    }

    //returns the index of the variable with the id passed
    private static int getVarIndex(String id){
        for(int i = 0; i < vars.size(); i++){
            if(vars.get(i).getID().equals(id)){
                return i;
            }
        }
        return -1;
    }

    //This method gets all data interpreter needs to run
    private static void getGlobals(File f){
        root = Parser.getRootNode(f);
        nodes = Parser.getNodes();
        //tokens = LexicalAnalyzer.getTokenList(f);
    }

    //adds a Variable to vars
    //creates an Error if the Variable already exists
    private static void addVariable(Variable v, Node n){
        if(varsContains(v.getID()) == null){
            vars.add(v);
        } else {
            createError("variable already exists", n.getFirstToken().getLexeme(), n.getFirstToken().getLine());
        }
    }

    //this method begins the Node processing
    //this essentially runs the source code
    private static void interpret(){
        //ensure root is not null
        if(root == null) {
            createError("root is null");
        //ensure root has children
        } else if(root.getChildren().isEmpty()){
            createError("root has 0 children");
        } else {
            //start interpreter
            Node start = root;
            int startChild = 0;
            while(!start.getFirstToken().checkType(Token.TokenType.EOF) && !errorOccurred){
                //call blocks
                block(start.getFirstChild());
                //iterate through root's children(blocks)
                startChild++;
                if(startChild < root.getChildren().size()){
                    start = root.getChild(startChild);
                }
            }
        }
    }

    //<block> -> <statement> | <statement> <block>
    private static void block(Node node){
        //traverse through any block or statement nodes
        while(node.getGrammar().equals("<block>") || node.getGrammar().equals("<statement>")){
            node = node.getFirstChild();
        }
        //determine which statement method to call
        switch(node.getGrammar()){
            case("<assignment_statement>"):
                assignI(node);
                break;
            case("<print_statement>"):
                printI(node);
                break;
            case("<if_statement>"):
                ifI(node);
                break;
            case("<while_statement>"):
                whileI(node);
                break;
            case("<repeat_statement>"):
                repeatI(node);
                break;
        }
    }

    //<assignment_statement> -> id <assignment_operator> <arithmetic_expression>
    private static void assignI(Node node){
        //get all node's children
        Node var = node.getFirstChild();
        Node op = node.getChild(1);
        Node expr = node.getChild(2);

        //temporary Variable object created to check if the Variable was initialized or not
        Variable tempVar = varsContains(var.getFirstToken().getLexeme());
        if(tempVar == null){
            //variable was not initialized
            //add new Variable with temporary value
            tempVar = new Variable(var.getFirstToken().getLexeme(), "");
            addVariable(tempVar, var);
        }
        //call arithmetic for expression
        Variable arithExpr = arithmetic(expr);
        //set ID of variable
        arithExpr.setID(tempVar.getID());
        //check if normal assignment or addition assignment
        if(op.getFirstToken().checkType(Token.TokenType.ASSIGN_OP)){
            vars.set(getVarIndex(arithExpr.getID()), arithExpr);
        } else if(op.getFirstToken().checkType(Token.TokenType.AE_OP)){
            int result = Integer.parseInt(vars.get(getVarIndex(arithExpr.getID())).getValue()) + Integer.parseInt(arithExpr.getValue());
            vars.set(getVarIndex(arithExpr.getID()), new Variable(arithExpr.getID(), String.valueOf(result)));
        }
    }

    //<print_statement> -> print ( <arithmetic_expression> )
    private static void printI(Node node){
        //stores the print statement's arithmetic expression node
        Node printExpr;
        //print depends on the number of children node has
        if(node.getChildren().size() == 1){
            printExpr = node.getFirstChild();
        } else {
            printExpr = node.getChild(2);
        }
        //temporary Variable for arithmetic expression
        Variable printVar = arithmetic(printExpr);
        if(printVar != null){
            //store print output in output
            output = output + printVar.getValue() + "\n";
        }
    }

    //<if_statement> -> if <boolean_expression> <block> else <block> end
    private static void ifI(Node node){
        //get child nodes
        Node boolExpr = node.getChild(1);
        Node thenBlock = node.getChild(2);
        Node elseBlock = node.getChild(4);

        //call bool to process expression
        boolean boolValue = bool(boolExpr);

        if(boolValue && !errorOccurred){
            block(thenBlock);
        } else if(!boolValue && !errorOccurred){
            block(elseBlock);
        }
    }

    //<while_statement> -> while <boolean_expression> <block> end
    private static void whileI(Node node){
        Node boolExpr = node.getChild(1);
        Node doBlock = node.getChild(2);

        boolean boolValue = bool(boolExpr);
        while(boolValue && !errorOccurred){
            block(doBlock);
            boolValue = bool(boolExpr);
        }
    }

    //<repeat_statement> -> repeat <block> until <boolean_expression>
    private static void repeatI(Node node){
        //get nodes children
        Node repeatBlock = node.getChild(1);
        Node boolExpr = node.getChild(3);
        //boolean to store result of boolean expression
        boolean boolValue = false;
        while (!boolValue && !errorOccurred){
            block(repeatBlock);
            boolValue = bool(boolExpr);
        }
    }

    //<boolean_expression> -> <arithmetic_expression> <relative_op> <arithmetic_expression>
    //returns the result of the boolean expression
    private static boolean bool(Node node){
        //get nodes children
        Node n1 = node.getFirstChild();
        Node op = node.getChild(1);
        Node n2 = node.getChild(2);

        //temporary Variables to hold arguments
        Variable arg1 = arithmetic(n1);
        Variable arg2 = arithmetic(n2);

        if(arg1 == null || arg2 == null){
            createError("invalid argument", "", node.getFirstToken().getLine());
            return false;
        }

        //store variable values
        int val1 = Integer.parseInt(arg1.getValue());
        int val2 = Integer.parseInt(arg2.getValue());

        //perform expression
        switch(op.getFirstChild().getGrammar()){
            case "le_operator":
                if(val1 <= val2)
                    return true;
                else
                    return false;
            case "lt_operator":
                if(val1 < val2)
                    return true;
                else
                    return false;
            case "ge_operator":
                if(val1 >= val2)
                    return true;
                else
                    return false;
            case "gt_operator":
                if(val1 > val2)
                    return true;
                else
                    return false;
            case "eq_operator":
                if(val1 == val2)
                    return true;
                else
                    return false;
            case "ne_operator":
                if(val1 != val2)
                    return true;
                else
                    return false;
            default:
                createError("unexpected operation", op.getFirstToken().getLexeme(), op.getFirstToken().getLine());
                return false;
        }

    }

    //<arithmetic_expression> -> <id> | <literal_integer> | <arithmetic_expression> <arithmetic_op> <arithmetic_expression>
    //returns a Variable (if no errors occurred) or null (if errors occurred)
    private static Variable arithmetic(Node node){
        //stores result of method
        Variable var;

        //if statement chooses what to do based on the number of children node has
        //if node has either 0-1 children the node is either a variable identifier of integer
        //node has 0 children
        if(node.getChildren().size() == 0){
            //if node is an identifier
            if(node.getGrammar().equals("id")){
                //ensure the variable has been initialized
                var = varsContains(node.getFirstToken().getLexeme());
                if(var != null) {
                    //variable was initialized
                    return var;
                } else {
                    //variable was not initialized
                    createError("variable not found", node.getFirstToken().getLexeme(), node.getFirstToken().getLine());
                    return null;
                }
            //if node is an integer
            } else if(node.getGrammar().equals("literal_integer")){
                return new Variable("temp", node.getFirstToken().getLexeme());
            //if node is neither identifier or integer
            } else {
                createError("incorrect value", node.getFirstToken().getLexeme(), node.getFirstToken().getLine());
                return null;
            }
        //node has 1 child
        } else if(node.getChildren().size() == 1){
            Node nChild = node.getFirstChild();
            if(nChild.getGrammar().equals("id")){
                //ensure the variable has been initialized
                var = varsContains(nChild.getFirstToken().getLexeme());
                if(var != null) {
                    //variable was initialized
                    return var;
                } else {
                    //variable was not initialized
                    createError("variable not found", nChild.getFirstToken().getLexeme(), nChild.getFirstToken().getLine());
                    return null;
                }
                //if node is an integer
            } else if(nChild.getGrammar().equals("literal_integer")){
                return new Variable("temp", nChild.getFirstToken().getLexeme());
                //if node is neither identifier or integer
            } else {
                createError("incorrect value", nChild.getFirstToken().getLexeme(), nChild.getFirstToken().getLine());
                return null;
            }
        //node has 3 children
        //expanded arithmetic expression
        } else if(node.getChildren().size() == 3){
            //get child nodes
            Node n1 = node.getFirstChild();
            Node op = node.getChild(1);
            Node n2 = node.getChild(2);

            //initialize arguments as temporary variables
            Variable arg1 = arithmetic(n1);
            Variable arg2 = arithmetic(n2);

            if(arg1 == null || arg2 == null){
                createError("invalid argument", "", node.getFirstToken().getLine());
                return null;
            }

            return operations(op, arg1, arg2);
        }
        //return null if method gets here, an error probably occurred
        createError("arithmetic expression error");
        return null;
    }

    //performs arithmetic operations
    //takes in node op(holds operation), and Variable arg1 and Variable arg2 (expressions arguments)
    //returns Variable object that stores result of method
    private static Variable operations(Node op, Variable arg1, Variable arg2){
        //store Variable values for convenience
        int val1 = Integer.parseInt(arg1.getValue());
        int val2 = Integer.parseInt(arg2.getValue());
        String result = "";
        //perform arithmetic operation
        switch (op.getGrammar()) {
            case "division_operator":
                //cannot divide by zero
                if(val2 != 0){
                    result = String.valueOf(val1 / val2);
                    break;
                } else
                    createError("cannot divide by zero", arg2.getValue(), op.getFirstToken().getLine());
                break;
            case "multiplication_operator":
                result = String.valueOf(val1 * val2);
                break;
            case "addition_operator":
                result = String.valueOf(val1 + val2);
                break;
            case "subtraction_operator":
                result = String.valueOf(val1 - val2);
                break;
            default:
                //operation not found
                createError("incorrect operation", op.getFirstToken().getLexeme(), op.getFirstToken().getLine());
                return null;
        }
        //return temporary variable that holds result
        return new Variable("temp", result);
    }

    public static void main(String args[]){
        File f = new File("src/Julia-Files/Test3.jl");
        getGlobals(f);
        if(nodes != null && root != null) {
            interpret();

            if(errorOccurred){
                System.out.println("Interpreter Errors:");
                for(Error e: errors){
                    e.printError();
                }
            } else {
                System.out.println("Lexical Analyzer Results\nSymbol Table:");

                System.out.println("\nParser Results\nAST:");
                Parser.printTree(root, nodes);

                System.out.println("\nInterpreter Results:\nOutput of Source Code File:");
                System.out.println(output);
            }
        } else if(nodes == null || tokens == null){
            if(nodes == null)
                createError("parser error");
        }
    }
}
