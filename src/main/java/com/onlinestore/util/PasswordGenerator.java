package com.onlinestore.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Пароль, который хотим захешировать
        String rawPassword = "admin123";
        
        // Генерируем хеш
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("Raw password: " + rawPassword);
        System.out.println("BCrypt hash: " + encodedPassword);
        
        // Проверка (для демонстрации)
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("Password matches: " + matches);
    }
}
