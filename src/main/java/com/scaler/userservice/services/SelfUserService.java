package com.scaler.userservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaler.userservice.dtos.SendEmailDto;
import com.scaler.userservice.exceptions.IncorrectPasswordException;
import com.scaler.userservice.exceptions.TokenExpiredException;
import com.scaler.userservice.exceptions.UserDoesNotExistException;
import com.scaler.userservice.models.Token;
import com.scaler.userservice.models.User;
import com.scaler.userservice.repositories.TokenRepository;
import com.scaler.userservice.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class SelfUserService implements UserService{
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    SelfUserService(BCryptPasswordEncoder bCryptPasswordEncoder,
                    UserRepository userRepository,
                    TokenRepository tokenRepository,
                    KafkaTemplate<String, String> kafkaTemplate,
                    ObjectMapper objectMapper){
        this.bCryptPasswordEncoder=bCryptPasswordEncoder;
        this.userRepository=userRepository;
        this.tokenRepository=tokenRepository;
        this.kafkaTemplate=kafkaTemplate;
        this.objectMapper=objectMapper;
    }

    @Override
    public User signUp(String name, String email, String password) {
        //create user

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setHashedPassword(bCryptPasswordEncoder.encode(password));
        //save to DB
        User savedUser= userRepository.save(user);
        SendEmailDto emailDto = new SendEmailDto();
        emailDto.setBody("Hope you have a nice journey");
        emailDto.setSubject("WelcomeToScaler");
        emailDto.setTo(savedUser.getEmail());
        emailDto.setFrom("xyz@xyz.com");
        try {
            kafkaTemplate.send(
                    "sendEmail",
                    objectMapper.writeValueAsString(emailDto));
        }catch (Exception e){
            System.out.println("some error message");
        }
        return savedUser;
    }

    @Override
    public Optional<Token> login(String email, String password) throws UserDoesNotExistException, IncorrectPasswordException {
        //check if user exists
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByEmail(email));
        //if not throw exception
        if(optionalUser.isEmpty()){
            throw new UserDoesNotExistException("User with email id: "+ email+ " does not exist" );
        }
        //user email exists, check is password matches
        User user = optionalUser.get();
        if(!bCryptPasswordEncoder.matches(password, user.getHashedPassword())){
            throw new IncorrectPasswordException("Password is incorrect");
        }
        //login success. generate a new Token
        //TODO can also check if tokens already exist and limit the no.of tokens.
        // Will need to increment the token count if created
        Token token = generateToken(user);
        //save token in DB
        return Optional.of(tokenRepository.save(token));
    }

    @Override
    public void logout(String token) throws TokenExpiredException {
        //check if token already deleted/expired
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeleted(token,true);
        if(optionalToken.isEmpty()){
            throw new TokenExpiredException("Token is already expired");
        }
        //if not then mark as delete
        Token token1 = optionalToken.get();
        token1.setDeleted(true);
        tokenRepository.save(token1);
        //TODO can decrement the token count if multiple tokens are allowed
    }

    @Override
    public Token generateToken(User user) {
        LocalDate currentDate = LocalDate.now();
        // Add 30 days to the current date
        LocalDate expiryDateTime = currentDate.plus(30, ChronoUnit.DAYS);
        Token token = new Token();
        token.setValue(RandomStringUtils.randomAlphanumeric(128));
        token.setUser(user);

        return token;
    }


    @Override
    public User validateToken(String token) {
        Optional<Token> optionalToken = tokenRepository.findByValueAndDeletedAndExpiryAtGreaterThan(token,false,new Date());
        if(optionalToken.isEmpty()){
            //throw exception
            return null;
        }

        return optionalToken.get().getUser();
    }
}
