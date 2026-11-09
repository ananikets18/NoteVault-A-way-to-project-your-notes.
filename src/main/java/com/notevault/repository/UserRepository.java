package com.notevault.repository;

import com.notevault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity persistence operations.
 * Demonstrates Java Core concepts:
 * - Interfaces: Declaring contract without implementation details.
 * - Generics: JpaRepository<User, Long> type parameters.
 * - Optional: Optional<User> return type to prevent NullPointerException.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
