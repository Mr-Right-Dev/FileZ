package dev.right.filez.services;

import dev.right.filez.Application;
import dev.right.filez.model.Item;
import dev.right.filez.model.User;
import dev.right.filez.repositorys.ItemRepository;
import dev.right.filez.repositorys.ShareTableRepository;
import dev.right.filez.repositorys.UserRepository;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
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

        String browserMime = file.getContentType();

        if (browserMime != null && !browserMime.equalsIgnoreCase(realMime)) {
            throw new SecurityException("Invalid mime.");
        }
    }

    public Item uploadItem(MultipartFile file, Long ownerId) throws IOException {
        validateFile(file);

        String storedPath;
        try (InputStream inputStream = file.getInputStream()) {
            storedPath = fileUploaderService.storeFile(inputStream, file.getOriginalFilename());
        }

        Item item = new Item();

        item.setItemPath(storedPath);
        item.setOwnerId(ownerId);
        item.setItemType(Item.ItemType.ITEM);
        item.setSize(file.getSize());

        itemRepository.save(item);

        return item;
    }

    public boolean canUserWriteOn(User user, Long parentId) throws FileNotFoundException {
        Item parent = itemRepository.loadItemById(parentId);
        if (parent == null) {
            throw new FileNotFoundException("Parent not found.");
        }

        if (parent.getOwnerId() == user.getUserId()) {
            return true;
        }

        User owner = userRepository.getUserById(parent.getOwnerId());
        if (owner == null) {
            // wat.
            throw new FileNotFoundException("Parent not found.");
        }
        if (!shareTableRepository.doesUserHaveAnySharedFileWithUser(owner, user)) {
            return false; // Saves many queries.
        }



        return false;
    }
}
