package com.onlinestore.repository;

import com.onlinestore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Найти пользователя по email
    Optional<User> findByEmail(String email);
    
    // Найти пользователя по username
    Optional<User> findByUsername(String username);
    
    // Проверить существует ли пользователь с таким email
    boolean existsByEmail(String email);
    
    // Проверить существует ли пользователь с таким username
    boolean existsByUsername(String username);
    
    // Найти всех активных пользователей
    List<User> findByIsActiveTrue();
    
    // Найти пользователей по роли
    List<User> findByRole(com.onlinestore.model.UserRole role);
    
    // Поиск пользователей по имени или фамилии
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName);
    
    // Найти пользователей по email (частичное совпадение)
    List<User> findByEmailContainingIgnoreCase(String email);
}
