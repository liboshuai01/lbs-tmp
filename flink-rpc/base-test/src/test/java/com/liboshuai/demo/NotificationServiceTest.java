package com.liboshuai.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

// 启用 Mockito 注解支持
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    // @Mock: 创建一个 UserRepository 的模拟（假）对象
    @Mock
    private UserRepository mockUserRepository;

    // @InjectMocks: 创建 NotificationService 实例, 并将上面 @Mock 注解的依赖自动注入进去
    @InjectMocks
    private NotificationService notificationService;

    @Test
    void shouldGenerateWelcomeMessageForExistingUser() {
        // Arrange (准备)
        // "打桩"：规定当调用 mockUserRepository 的 findUsernameById 方法并传入 "user123" 时，
        // 应该返回 "Alice"。我们不需要 UserRepository 的真实实现。
        when(mockUserRepository.findUsernameById("user123")).thenReturn("Alice");

        // Act (执行)
        String message = notificationService.generateWelcomeMessage("user123");

        // Assert (断言)
        assertThat(message).isEqualTo("Welcome, Alice!");
    }

    @Test
    void shouldGenerateGuestMessageForNonExistingUser() {
        // Arrange
        // "打桩"：规定当调用 findUsernameById 时，返回 null
        when(mockUserRepository.findUsernameById("unknownUser")).thenReturn(null);

        // Act
        String message = notificationService.generateWelcomeMessage("unknownUser");

        // Assert
        assertThat(message).isEqualTo("Welcome, Guest!");
    }
}