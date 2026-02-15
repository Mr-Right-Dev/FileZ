package dev.right.filez.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileInterface {
    @GetMapping("/{fileId}")
    public ResponseEntity<?> download(
            @PathVariable long fileId,
            Authentication auth
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestBody MultipartFile file
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .build();
    }
}
