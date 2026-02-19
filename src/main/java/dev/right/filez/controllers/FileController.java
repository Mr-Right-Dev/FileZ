package dev.right.filez.controllers;

import dev.right.filez.Application;
import dev.right.filez.model.*;
import dev.right.filez.repositorys.ItemRepository;
import dev.right.filez.repositorys.UserRepository;
import dev.right.filez.repositorys.WorkspaceRepository;
import dev.right.filez.services.FileHandlerService;
import dev.right.filez.services.FilePermissionService;
import dev.right.filez.services.FileUploaderService;
import dev.right.filez.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/file")
public class FileController {
    private final FilePermissionService filePermissionService;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final FileHandlerService fileHandlerService;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final FileUploaderService fileUploaderService;

    @Autowired
    public FileController(FilePermissionService filePermissionService, UserRepository userRepository, UserService userService, ItemRepository itemRepository, FileHandlerService fileHandlerService, WorkspaceRepository workspaceRepository, FileUploaderService fileUploaderService) {
        this.filePermissionService = filePermissionService;
        this.userService = userService;
        this.itemRepository = itemRepository;
        this.fileHandlerService = fileHandlerService;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.fileUploaderService = fileUploaderService;
    }

    @GetMapping("/{fileId}/data")
    public ResponseEntity<?> getData(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long fileId
    ) {
        Item item = itemRepository.loadItemById(fileId);
        if (item == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        User refreshRequester = userService.getRefreshUser(userPrincipal.getUser());
        if (refreshRequester == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        try {
            if (!filePermissionService.canUserOn(refreshRequester, item, ShareTable.AccessType.READ, item.getWorkspaceId())) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .build();
            }
        } catch (FileNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        return ResponseEntity
                .ok(
                        item.toMap()
                );
    }

    @GetMapping("/{fileId}/folder")
    public void downloadFolder(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long fileId,
            HttpServletResponse response
    ) throws IOException {
        Item item = itemRepository.loadItemById(fileId);
        if (item == null) {
            response.setStatus(404);
            return;
        }
        if (item.getItemType() == Item.ItemType.ITEM) {
            response.setStatus(404);
            return;
        }

        User refreshRequester = userService.getRefreshUser(userPrincipal.getUser());
        if (refreshRequester == null) {
            response.setStatus(401);
            return;
        }

        try {
            if (!filePermissionService.canUserOn(refreshRequester, item, ShareTable.AccessType.READ, item.getWorkspaceId())) {
                response.setStatus(404);
                return;
            }
        } catch (FileNotFoundException e) {
            response.setStatus(404);
            return;
        }

       fileHandlerService.streamFolderAsZip(response, item);
    }

    @GetMapping("/{fileId}/resource")
    public ResponseEntity<?> download(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable long fileId
    ) {

        Item item = itemRepository.loadItemById(fileId);
        if (item == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        if (item.getItemType() == Item.ItemType.FOLDER) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        User refreshRequester = userService.getRefreshUser(userPrincipal.getUser());
        if (refreshRequester == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        try {
            if (!filePermissionService.canUserOn(refreshRequester, item, ShareTable.AccessType.READ, item.getWorkspaceId())) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .build();
            }
        } catch (FileNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }

        try {
            UrlResource resource = fileUploaderService.getFileResource(item.getItemPath());

            if (item.getMimeType() == null) {
                item.setMimeType("");
            }

            return ResponseEntity
                    .ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + item.getItemName() + "\""
                    )
                    .contentType(item.getMimeType().isEmpty() ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(item.getMimeType()))
                    .contentLength(item.getSize())
                    .body(resource);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
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
