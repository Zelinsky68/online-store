package com.onlinestore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OnlineStoreApplicationTests {

    @Test
    void contextLoads() {
        // Тест проверяет, что контекст Spring успешно загружается
    }

    @Test
    void applicationStarts() {
        // Простой тест для проверки, что приложение запускается
        OnlineStoreApplication.main(new String[]{});
        assertThat(true).isTrue();
    }
}
