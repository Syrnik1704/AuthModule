package com.example.auth.repository;

import com.example.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String login);
    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByUuid(String uuid);
    @Query(nativeQuery = true, value = "SELECT * FROM users WHERE login=?1 AND lock=false AND enabled=true")
    Optional<User> findUserByLoginAndLockAndEnabled(String login);
}
