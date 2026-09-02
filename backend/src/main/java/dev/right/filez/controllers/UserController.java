package dev.right.filez.controllers;

import dev.right.filez.model.User;
import dev.right.filez.model.UserPrincipal;
import dev.right.filez.repositorys.UserRepository;
import dev.right.filez.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        User user = userService.getRefreshUser(userPrincipal.getUser());
        return ResponseEntity
                .ok(
                        user.toMap()
                );
    }

    @GetMapping("/get/{userId}")
    public ResponseEntity<?> getUserInfo(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long userId
    ) {
        if (userService.getRefreshUser(userPrincipal.getUser()).getAccessLevel() == User.AccessLevel.NORMAL) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .build();
        }

        User user = userRepository.getUserById(userId);
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseEntity
                .ok(
                        user.toString()
                );
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return ResponseEntity
                    .status(HttpStatus.NOT_IMPLEMENTED)
                .build();
    }
}
