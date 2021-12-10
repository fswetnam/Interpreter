/*
Class:       CS 4308 Section 03
Term:        Fall 2021
Name:       Faith Swetnam
Instructor:   Sharon Perry
Project:     Deliverable 1 Scanner
 */

//Class to store error objects
//An error object stores an error message, the value that caused the error (if applicable) and the line the error
//occurred on
public class Error {
    private String msg = "";
    private String value = null;
    private int line = 0;

    //Error object if value is available
    Error(String msg, String value, int line){
        this.msg = msg;
        this.value = value;
        this.line = line;
    }

    //Error object if value unavailable
    Error(String msg, int line){
        this.msg = msg;
        this.line = line;
    }

    //Error object if no value or line if specified
    Error(String msg){
        this.msg = msg;
    }

    //Prints the error object values out
    void printError(){
        System.out.printf("%-50s\t%-10s\t%-10d\n",msg, value, line);
    }
}
