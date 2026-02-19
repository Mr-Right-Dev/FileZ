package dev.right.filez.services;

import dev.right.filez.Application;
import dev.right.filez.model.Item;
import dev.right.filez.model.User;
import dev.right.filez.repositorys.ItemRepository;
import dev.right.filez.repositorys.ShareTableRepository;
import dev.right.filez.repositorys.UserRepository;
import jakarta.annotation.Nullable;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.apache.commons.io.FilenameUtils.getExtension;

@Service
public class FileHandlerService {
    private final ItemRepository itemRepository;
    private final FileUploaderService fileUploaderService;
    private final Tika tika = new Tika();
    private final UserRepository userRepository;
    private final ShareTableRepository shareTableRepository;

    @Autowired
    public FileHandlerService(ItemRepository itemRepository, FileUploaderService fileUploaderService, UserRepository userRepository, ShareTableRepository shareTableRepository) {
        this.itemRepository = itemRepository;
        this.fileUploaderService = fileUploaderService;
        this.userRepository = userRepository;
        this.shareTableRepository = shareTableRepository;
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new SecurityException("Empty file.");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.contains("..")) {
            throw new SecurityException("Invalid filename.");
        }

        String realMime = tika.detect(file.getInputStream());

        if (Application.fileTypesWhitelist) {
            String cleanName = Path.of(originalName)
                    .getFileName()
                    .toString();
            String ext = getExtension(cleanName);

            if (!Application.fileWhitelistExt.contains(ext)) {
                throw new SecurityException("Invalid extension.");
            }

            if (!Application.fileWhitelistMimeTypes.contains(realMime)) {
                throw new SecurityException("Invalid mime.");
            }
        }

        /*String browserMime = file.getContentType();

        if (browserMime != null && !browserMime.equalsIgnoreCase(realMime)) {
            throw new SecurityException("Invalid mime: "+browserMime);
        }*/
    }

    public Item uploadItem(MultipartFile file, Long ownerId, Long workspaceId, String originalName) throws IOException {
        validateFile(file);


        String storedPath;
        try (InputStream inputStream = file.getInputStream()) {
            storedPath = fileUploaderService.storeFile(inputStream, originalName);
        }

        Item item = new Item();

        item.setItemPath(storedPath);
        item.setOwnerId(ownerId);
        item.setItemType(Item.ItemType.ITEM);
        item.setSize(file.getSize());
        item.setWorkspaceId(workspaceId);
        item.setItemName(originalName);

        Long id = itemRepository.save(item);
        item.setItemId(id);

        return item;
    }

    public Item saveFileAndFolders(MultipartFile file, @Nullable Long parentId, User sender, Long workspaceId) throws IOException, NullPointerException {
        String relativePath = file.getOriginalFilename();

        String[] parts = relativePath.split("/");

        @Nullable
        Long folderParentId = parentId;

        for (int i = 0; i < parts.length; i++) {
            String folderName = parts[i];

            Item folder = new Item();

            folder.setParentId(parentId);
            folder.setOwnerId(sender.getUserId());
            folder.setItemType(Item.ItemType.FOLDER);
            folder.setItemName(folderName);
            folder.setWorkspaceId(workspaceId);

            folderParentId = itemRepository.save(folder);
        }

        return uploadItem(file, sender.getUserId(), workspaceId, parts[parts.length-1]);
    }
}
