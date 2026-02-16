package dev.right.filez.services;

import dev.right.filez.model.User;
import dev.right.filez.model.UserPrincipal;
import dev.right.filez.repositorys.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Autowired
    public OAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String emailFromGoogle = (String) attributes.get("email");
        if (emailFromGoogle == null) {
            throw new OAuth2AuthenticationException("Invalid credentials.");
        }

        User user = userRepository.getUserByEmail(emailFromGoogle);
        if (user == null) {
            throw new OAuth2AuthenticationException("Invalid credentials.");
        }

        UserPrincipal principal = new UserPrincipal(user);
        principal.setAttributes(oAuth2User.getAttributes());

        return principal;
    }
}
