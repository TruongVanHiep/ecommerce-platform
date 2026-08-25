package com.dev.E_commerce.Mini.repository;

import com.dev.E_commerce.Mini.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsUserByUsername(String username);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}
