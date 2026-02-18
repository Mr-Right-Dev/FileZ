package dev.right.filez.controllers;

import dev.right.filez.model.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {
    @GetMapping("/{fileId}")
    public ResponseEntity<?> download(
            @PathVariable long fileId
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("files") MultipartFile file,
            @RequestParam Long parentId
    ) {
        // I'm gonna use 0 as workspace.
        try {
            
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        return ResponseEntity
                .status(HttpStatus.NOT_IMPLEMENTED)
                .build();
    }
}
