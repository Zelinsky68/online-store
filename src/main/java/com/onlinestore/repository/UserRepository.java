package com.onlinestore.repository;

import com.onlinestore.model.User;
import com.onlinestore.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ============= БАЗОВЫЕ МЕТОДЫ ПОИСКА =============
    
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    // ============= ПОИСК ПО СТАТУСУ =============
    
    List<User> findByIsActiveTrue();
    List<User> findByIsActiveFalse();
    
    // ============= ПОИСК ПО РОЛЯМ =============
    
    List<User> findByRole(UserRole role);
    
    // ============= ПОИСК ПО ИМЕНИ/EMAIL =============
    
    List<User> findByFirstNameContainingIgnoreCase(String firstName);
    List<User> findByLastNameContainingIgnoreCase(String lastName);
    
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
    
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String username, String email);
    
    // ============= МЕТОД SEARCH BY NAME (ДЛЯ USER SERVICE) =============
    
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByName(@Param("searchTerm") String searchTerm);
    
    // ============= ПОЛНОТЕКСТОВЫЙ ПОИСК =============
    
    @Query(value = "SELECT * FROM users WHERE " +
           "to_tsvector('russian', coalesce(username, '') || ' ' || " +
           "coalesce(first_name, '') || ' ' || coalesce(last_name, '') || ' ' || " +
           "coalesce(email, '')) @@ to_tsquery('russian', :query)", 
           nativeQuery = true)
    List<User> fullTextSearch(@Param("query") String query);
    
    // ============= ПОИСК ПО ДАТАМ =============
    
    List<User> findByCreatedAtAfter(LocalDateTime date);
    List<User> findByCreatedAtBefore(LocalDateTime date);
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<User> findByLastLoginAfter(LocalDateTime date);
    List<User> findByLastLoginIsNull();
    
    @Query("SELECT u FROM User u WHERE u.lastLogin < :date OR u.lastLogin IS NULL")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);
    
    // ============= СТАТИСТИКА =============
    
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUsersByRole();
    
    @Query("SELECT u.role, COUNT(u) FROM User u WHERE u.isActive = true GROUP BY u.role")
    List<Object[]> countActiveUsersByRole();
    
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBefore(LocalDateTime date);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT u.isActive, COUNT(u) FROM User u GROUP BY u.isActive")
    List<Object[]> countUsersByActivity();
    
    // ============= СОРТИРОВКА =============
    
    List<User> findAllByOrderByCreatedAtDesc();
    List<User> findAllByOrderByUsernameAsc();
    List<User> findAllByOrderByEmailAsc();
    List<User> findAllByOrderByLastLoginDesc();
    
    // ============= УДАЛЕНИЕ =============
    
    void deleteByEmail(String email);
    void deleteByUsername(String username);
    
    @Query("DELETE FROM User u WHERE u.isActive = false")
    void deleteByIsActiveFalse();
    
    // ============= ПРОВЕРКИ =============
    
    boolean existsByRoleAndIsActiveTrue(UserRole role);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
           "WHERE u.email = :email AND u.id != :id")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u " +
           "WHERE u.username = :username AND u.id != :id")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("id") Long id);
}
