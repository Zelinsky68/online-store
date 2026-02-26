package com.onlinestore.controller.admin;

import com.onlinestore.dto.UserDto;
import com.onlinestore.model.User;
import com.onlinestore.model.UserRole;
import com.onlinestore.repository.OrderRepository;
import com.onlinestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ============= GET METHODS =============

    // Получить всех пользователей
    @GetMapping
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Получить пользователя по ID
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(convertToDto(user), HttpStatus.OK);
    }

    // Поиск пользователей
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(userDtos, HttpStatus.OK);
    }

    // Получить всех активных пользователей
    @GetMapping("/active")
    public List<UserDto> getActiveUsers() {
        return userRepository.findByIsActiveTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Получить всех неактивных пользователей
    @GetMapping("/inactive")
    public List<UserDto> getInactiveUsers() {
        return userRepository.findByIsActiveFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Получить пользователей по роли
    @GetMapping("/role/{role}")
    public List<UserDto> getUsersByRole(@PathVariable UserRole role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Получить заказы пользователя
    @GetMapping("/{id}/orders")
    public ResponseEntity<?> getUserOrders(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user.getOrders(), HttpStatus.OK);
    }

    // ============= CREATE METHODS =============

    // Создать нового пользователя (админом)
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        // Проверка на существование
        if (userRepository.findByUsername(userDto.getUsername()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Username already exists: " + userDto.getUsername());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email already exists: " + userDto.getEmail());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        
        // Устанавливаем временный пароль (нужно будет сменить при первом входе)
        String defaultPassword = "changeme123";
        user.setPassword(passwordEncoder.encode(defaultPassword));
        
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhone(userDto.getPhone());
        user.setRole(userDto.getRole() != null ? userDto.getRole() : UserRole.CUSTOMER);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        return new ResponseEntity<>(convertToDto(savedUser), HttpStatus.CREATED);
    }

    // ============= UPDATE METHODS =============

    // Изменить роль пользователя
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDto> changeUserRole(
            @PathVariable Long id,
            @RequestParam UserRole role) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return new ResponseEntity<>(convertToDto(updatedUser), HttpStatus.OK);
    }

    // Переключить активность пользователя (блокировка/разблокировка)
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<UserDto> toggleUserActive(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return new ResponseEntity<>(convertToDto(updatedUser), HttpStatus.OK);
    }

    // Активировать пользователя
    @PatchMapping("/{id}/activate")
    public ResponseEntity<UserDto> activateUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return new ResponseEntity<>(convertToDto(updatedUser), HttpStatus.OK);
    }

    // Деактивировать пользователя
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<UserDto> deactivateUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        User updatedUser = userRepository.save(user);
        return new ResponseEntity<>(convertToDto(updatedUser), HttpStatus.OK);
    }

    // Сбросить пароль пользователя
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<?> resetPassword(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        String newPassword = "reset123";
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successful");
        response.put("temporaryPassword", newPassword);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ============= DELETE METHODS =============

    // Удалить пользователя (осторожно!)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // Проверяем, есть ли у пользователя заказы
        if (!user.getOrders().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        userRepository.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ============= STATISTICS =============

    // Получить статистику по пользователям
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);
        
        long activeUsers = userRepository.findByIsActiveTrue().size();
        stats.put("activeUsers", activeUsers);
        
        long inactiveUsers = userRepository.findByIsActiveFalse().size();
        stats.put("inactiveUsers", inactiveUsers);
        
        // Статистика по ролям
        List<Object[]> roleCounts = userRepository.countUsersByRole();
        Map<String, Long> roleStats = new HashMap<>();
        for (Object[] row : roleCounts) {
            roleStats.put(((UserRole) row[0]).name(), (Long) row[1]);
        }
        stats.put("usersByRole", roleStats);
        
        // Пользователи за последние 7 дней
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long newUsers = userRepository.countByCreatedAtAfter(weekAgo);
        stats.put("newUsersLastWeek", newUsers);
        
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    // ============= HELPER METHODS =============

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }
}
