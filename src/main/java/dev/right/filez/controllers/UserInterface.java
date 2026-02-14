package dev.right.filez.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserInterface {
    @GetMapping("/get")
    public ResponseEntity<?> getUserInfo(
            Authentication auth
    ) {


        return ResponseEntity
                .ok(
                        Map.ofEntries(
                                Map.entry("username", auth.getName())
                        )
                );
    }
}
