package com.scaler.userservice.repositories;

import com.scaler.userservice.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.xml.crypto.Data;
import java.util.Date;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token,Long> {
    @Override
    Token  save(Token token);

    Optional<Token> findByValueAndDeleted(String  token, boolean isDeleted);

    Optional<Token> findByValueAndDeletedAndExpiryAtGreaterThan(String  token, boolean isDeleted, Date currentTime);
}
