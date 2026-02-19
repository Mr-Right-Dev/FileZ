package dev.right.filez.services;

import dev.right.filez.Application;
import dev.right.filez.model.Item;
import dev.right.filez.model.User;
import dev.right.filez.repositorys.ItemRepository;
import dev.right.filez.repositorys.ShareTableRepository;
import dev.right.filez.repositorys.UserRepository;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return lastDot == -1 ? "" : fileName.substring(lastDot+1);
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
            String ext = getFileExtension(cleanName);

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

    public Item uploadItem(MultipartFile file, Long ownerId, Long workspaceId, String originalName, @Nullable Long parentId) throws IOException {
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
        item.setParentId(parentId);

        Long id = itemRepository.save(item);
        item.setItemId(id);

        return item;
    }

    public Item saveFileAndFolders(MultipartFile file, @Nullable Long parentId, User sender, Long workspaceId) throws IOException, NullPointerException {
        String relativePath = file.getOriginalFilename();

        String[] parts = relativePath.split("/");

        @Nullable
        Long folderParentId = parentId;

        for (int i = 0; i < parts.length - 1; i++) {
            String folderName = parts[i];

            Item existingFolder = itemRepository.getItemByNameAndParentId(folderName, folderParentId, workspaceId);

            if (existingFolder == null) {
                Item folder = new Item();

                folder.setParentId(folderParentId);
                folder.setOwnerId(sender.getUserId());
                folder.setItemType(Item.ItemType.FOLDER);
                folder.setItemName(folderName);
                folder.setWorkspaceId(workspaceId);

                folderParentId = itemRepository.save(folder);
            } else {
                folderParentId = existingFolder.getItemId();
            }
        }

        return uploadItem(file, sender.getUserId(), workspaceId, parts[parts.length-1], folderParentId);
    }

    private String buildPath(Item item, Map<Long, Item> allItems) {

        StringBuilder path = new StringBuilder(item.getItemName());

        Long parentId = item.getParentId();

        while(parentId != null){

            Item parent = allItems.get(parentId);

            path.insert(0, parent.getItemName() + "/");

            parentId = parent.getParentId();
        }

        return path.toString();
    }

    public void streamFolderAsZip( HttpServletResponse response, Item item) throws IOException {
        response.setContentType("application/zip");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"folder.zip\""
        );

        List<Item> items = itemRepository.getDescendants(item);
        Map<Long, Item> map =
                items.stream()
                        .collect(Collectors.toMap(
                                Item::getItemId,
                                Function.identity()
                        ));

        ZipOutputStream zip =
                new ZipOutputStream(response.getOutputStream());

        for(Item itemObj : items) {

            String path = buildPath(item, map);

            if (itemObj.getItemType() == Item.ItemType.FOLDER) {

                zip.putNextEntry(
                        new ZipEntry(path + "/")
                );

                zip.closeEntry();

            } else {
                if (item.getItemPath() == null) {
                    continue;
                }

                zip.putNextEntry(
                        new ZipEntry(path)
                );

                InputStream fileStream =
                        fileUploaderService.getFileResource(item.getItemPath()).getInputStream();

                fileStream.transferTo(zip);

                zip.closeEntry();
            }
        }

        zip.finish();
    }
}
