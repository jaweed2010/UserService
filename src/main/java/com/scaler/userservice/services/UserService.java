package com.scaler.userservice.services;

import com.scaler.userservice.exceptions.IncorrectPasswordException;
import com.scaler.userservice.exceptions.TokenExpiredException;
import com.scaler.userservice.exceptions.UserDoesNotExistException;
import com.scaler.userservice.models.Token;
import com.scaler.userservice.models.User;

import java.util.Optional;

public interface UserService {
    public User signUp(String name, String email, String password);

    public Optional<Token> login(String name, String password) throws UserDoesNotExistException, IncorrectPasswordException;

    public void logout(String token) throws TokenExpiredException;


    public Token generateToken(User user);

    public User validateToken(String token);
}
