package dev.right.filez.services;

import dev.right.filez.repositorys.UserRepository;
import dev.right.filez.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthProvider(User.AuthProvider.LOCAL);
        userRepository.save(user);
    }

    public User getRefreshUser(User user) {
        return userRepository.getUserById(user.getUserId());
    }
}
