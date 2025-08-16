package com.liboshuai.demo;

// 这是我们要测试的类，它依赖 UserRepository
public class NotificationService {
    private final UserRepository userRepository;

    public NotificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateWelcomeMessage(String userId) {
        String username = userRepository.findUsernameById(userId);
        if (username == null) {
            return "Welcome, Guest!";
        }
        return "Welcome, " + username + "!";
    }
}