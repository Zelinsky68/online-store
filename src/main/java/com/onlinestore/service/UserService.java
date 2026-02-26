package com.onlinestore.service;

import com.onlinestore.model.User;
import com.onlinestore.model.UserRole;
import com.onlinestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.onlinestore.dto.UserRegistrationDto;
import com.onlinestore.dto.UserResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ============= GET METHODS =============
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<User> getActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> searchUsersByName(String searchTerm) {
        return userRepository.searchByName(searchTerm);
    }

    // ============= CREATE / UPDATE =============

    @Transactional
    public User createUser(User user) {
        // Проверка уникальности
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }
        
        // Шифруем пароль если есть
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        if (user.getIsActive() == null) {
            user.setIsActive(true);
        }
        if (user.getRole() == null) {
            user.setRole(UserRole.CUSTOMER);
        }
        
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // Обновляем только разрешенные поля
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());
        existingUser.setPhone(userDetails.getPhone());
        
        // Не обновляем email, username, пароль через этот метод
        
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(existingUser);
    }

    @Transactional
    public User changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    // ============= STATUS METHODS =============

    @Transactional
    public User deactivateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    @Transactional
    public User activateUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    @Transactional
    public User updateLastLogin(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        user.setLastLogin(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    // ============= CHECK METHODS =============

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    // ============= AUTHENTICATION =============

    public User authenticate(String usernameOrEmail, String password) {
        // Ищем по username или email
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        
        if (userOpt.isEmpty()) {
            return null;
        }
        
        User user = userOpt.get();
        
        // Проверяем пароль
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        
        // Проверяем активен ли пользователь
        if (!user.getIsActive()) {
            return null;
        }
        
        // Обновляем last_login
        updateLastLogin(user.getId());
        
        return user;
    }

    // ============= DELETE =============

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);

    }
    // ============= REGISTRATION =============

    @Transactional
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        // Проверка уникальности email
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already registered: " + registrationDto.getEmail());
        }

        // Проверка уникальности username
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already taken: " + registrationDto.getUsername());
        }

        // Создание нового пользователя
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        
        // Шифруем пароль
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());
        user.setPassword(encodedPassword);
        
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhone(registrationDto.getPhone());
        
        // Значения по умолчанию
        user.setIsActive(true);
        user.setRole(UserRole.CUSTOMER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Сохраняем в БД
        User savedUser = userRepository.save(user);

        // Конвертируем в DTO для ответа
        return convertToDto(savedUser);
    }

    private UserResponseDto convertToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
