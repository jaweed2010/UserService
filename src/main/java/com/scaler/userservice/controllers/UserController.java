package com.scaler.userservice.controllers;

import com.scaler.userservice.dtos.LoginRequestDto;
import com.scaler.userservice.dtos.LogoutRequestDto;
import com.scaler.userservice.dtos.SignUpRequestDto;
import com.scaler.userservice.dtos.UserDto;
import com.scaler.userservice.exceptions.IncorrectPasswordException;
import com.scaler.userservice.exceptions.TokenExpiredException;
import com.scaler.userservice.exceptions.UserDoesNotExistException;
import com.scaler.userservice.models.Token;
import com.scaler.userservice.models.User;
import com.scaler.userservice.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    UserController(UserService userService){
        this.userService=userService;
    }

    @PostMapping("/signup")
    public UserDto signUp(@RequestBody SignUpRequestDto requestDto){
        User user = userService.signUp(
                requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPassword()
        );
        return  UserDto.from(user);
    }

    @PostMapping("/login")
    public Optional<Token> login(@RequestBody LoginRequestDto loginRequestDto) throws IncorrectPasswordException, UserDoesNotExistException {
        Optional<Token> token = userService.login(
                loginRequestDto.getEmail(),
                loginRequestDto.getPassword()
        );

        return token;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(LogoutRequestDto logoutRequestDto) throws TokenExpiredException {
       try {
           userService.logout(
                   logoutRequestDto.getToken()
           );
           return new ResponseEntity<>(HttpStatus.OK);
       }
       catch (TokenExpiredException  e){
           return  new ResponseEntity<>(HttpStatus.BAD_REQUEST);
       }
    }

    @GetMapping("/validate/{token}")
    public UserDto validateToken(@PathVariable String token){
        User user = userService.validateToken(token);
        return UserDto.from(user);
    }

    @GetMapping ("/users/{id}")
    public UserDto getUserById(@PathVariable long id){
        return null;
    }
}
