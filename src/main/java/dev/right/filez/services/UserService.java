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
    private BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthProvider(User.AuthProvider.LOCAL);
        userRepository.save(user);
    }

    @Transactional
    public void processOAuthPostLogin(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.getUserByEmail(email);
        if (user != null) {
            return;
        }

        user = new User();
        user.setEmail(email);
        user.setUsername(oAuth2User.getAttribute("name"));
        user.setAuthProvider(User.AuthProvider.GOOGLE);
        userRepository.save(user);
    }
}
