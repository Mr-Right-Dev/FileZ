package dev.right.filez.controllers;

import dev.right.filez.Application;
import dev.right.filez.model.*;
import dev.right.filez.repositorys.ItemRepository;
import dev.right.filez.repositorys.UserRepository;
import dev.right.filez.repositorys.WorkspaceRepository;
import dev.right.filez.services.FileHandlerService;
import dev.right.filez.services.FilePermissionService;
import dev.right.filez.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
public class FileController {
    private final FilePermissionService filePermissionService;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final FileHandlerService fileHandlerService;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Autowired
    public FileController(FilePermissionService filePermissionService, UserRepository userRepository, UserService userService, ItemRepository itemRepository, FileHandlerService fileHandlerService, WorkspaceRepository workspaceRepository) {
        this.filePermissionService = filePermissionService;
        this.userService = userService;
        this.itemRepository = itemRepository;
        this.fileHandlerService = fileHandlerService;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
    }

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
            @RequestParam("files") MultipartFile[] files,
            @RequestParam Long parentId,
            @RequestParam Long workspaceId
    ) {
        if (parentId == 0) {
            parentId = null;
        }

        if (workspaceId == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        if (workspaceId == 0) {
            try {
                workspaceId = workspaceRepository.getOrCreateWorkspaceForUser(userPrincipal.getUser().getUserId());
            } catch (Exception e) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .build();
            }
        }

        // I'm gonna use 0 as workspace.

        User requester = userService.getRefreshUser(userPrincipal.getUser());
        Item parent = itemRepository.loadItemById(parentId);
        if (parent != null) {
            if (parent.getItemType() != Item.ItemType.FOLDER) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
            }
        }

        UserWorkspace workspace = workspaceRepository.getByWorkspaceId(workspaceId);

        User workspaceOwner;
        try {
            workspaceOwner = userRepository.getUserById(workspace.getUserId());
        } catch (NullPointerException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }

        try {
            if (!filePermissionService.canUserOn(requester, parent, ShareTable.AccessType.READ_WRITE, workspaceId)) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .build();
                // Not saying that is forbidden to not give a trace of this file.
            }
        } catch (FileNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        ArrayList<String> successList = new ArrayList<>();
        ArrayList<String> failedList = new ArrayList<>();

        long totalSize = 0L;
        long cap = Long.MAX_VALUE;
        boolean hasCap = false;
        try {
            Long workspaceCap = workspaceOwner.getFilesTotalSizeCap();
            if (workspaceCap != null) {
                if (workspaceCap != 0) {
                    System.out.println(workspaceCap);
                    cap = workspaceCap - workspaceOwner.getAccumulatedFileSize();
                    System.out.println(cap);
                    hasCap = true;
                }

            }
        } catch (NullPointerException e) {

        }

        for (MultipartFile file : files) {
            try {
                if (file.isEmpty()) {
                    failedList.add(file.getOriginalFilename());
                    continue;
                };

                if (hasCap) {
                    if ((file.getSize()+totalSize) >= cap) {
                        failedList.add(file.getOriginalFilename());
                        continue;
                    }
                }

                totalSize += file.getSize();

                Item item = fileHandlerService.saveFileAndFolders(file, parentId, requester, workspaceId);

                successList.add(file.getOriginalFilename());
            } catch (Exception e) {
                e.printStackTrace();
                failedList.add(file.getOriginalFilename());
            }
        }

        userRepository.incrementStorageUsageValue(totalSize, workspaceOwner);

        if (failedList.size() == files.length) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body( Map.ofEntries(
                            Map.entry("successfully", successList.toArray()),
                            Map.entry("failed", failedList)
                    ));
        }
        return ResponseEntity
                .ok(
                        Map.ofEntries(
                                Map.entry("successfully", successList.toArray()),
                                Map.entry("failed", failedList)
                        )
                );
    }
}
