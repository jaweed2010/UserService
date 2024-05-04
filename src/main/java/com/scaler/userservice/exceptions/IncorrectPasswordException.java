package com.scaler.userservice.exceptions;

public class IncorrectPasswordException extends Exception{
    public IncorrectPasswordException(String message){
         super(message);
    }
}
