package com.enterprise.employees.exception;

public class FieldTooLongException  extends RuntimeException{

    public FieldTooLongException(String message) {
        super(message);
    }

}
