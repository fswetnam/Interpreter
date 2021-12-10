/*
Class:       CS 4308 Section 03
Term:        Fall 2021
Name:       Faith Swetnam
Instructor:   Sharon Perry
Project:     Deliverable 2 Parser
Updated: 11/14/2021
 */

import java.util.ArrayList;
public class Node {
    private ArrayList<Token> tokens; //stores all the Token objects associated with the Node
    private String grammar; //stores the String grammar
    private Node parent; //stores the Node's parent
    private ArrayList<Node> children; //stores the Node's children

    //Node constructors
    Node(Token token, String grammar){
        this.tokens = new ArrayList<Token>();
        tokens.add(token);
        this.children = new ArrayList<Node>();
        this.parent = new Node();
        this.grammar = grammar;
    }

    Node(Token token, Node parent, String grammar){
        this.tokens = new ArrayList<Token>();
        tokens.add(token);
        this.children = new ArrayList<Node>();
        this.parent = parent;
        this.grammar = grammar;
    }

    Node(){
        this.tokens = new ArrayList<Token>();
        this.children = new ArrayList<Node>();
        this.parent = null;
        this.grammar = "";
    }

    //function to return the grammar of a Node
    String getGrammar(){ return grammar; }
    //function to return the Node's ArrayList of children
    ArrayList<Node> getChildren(){ return children; }
    //function to add a child to the Node
    void addChild(Node child){ children.add(child); }
    //function to add a Token to the Node
    void addToken(Token token){ this.tokens.add(token); }
    //function to return tokens
    ArrayList<Token> getTokens(){ return tokens; }
    Token getFirstToken(){ return tokens.get(0); }
    Node getFirstChild(){ return children.get(0); }
    Node getChild(int index) { return children.get(index); }

}
