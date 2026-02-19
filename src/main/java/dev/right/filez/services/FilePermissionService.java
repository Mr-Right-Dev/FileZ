package dev.right.filez.services;

import dev.right.filez.model.Item;
import dev.right.filez.model.ShareTable;
import dev.right.filez.model.User;
import dev.right.filez.model.UserWorkspace;
import dev.right.filez.repositorys.ItemRepository;
import dev.right.filez.repositorys.ShareTableRepository;
import dev.right.filez.repositorys.UserRepository;
import dev.right.filez.repositorys.WorkspaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.List;

@Service
public class FilePermissionService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ShareTableRepository shareTableRepository;
    private final WorkspaceRepository workspaceRepository;

    @Autowired
    public FilePermissionService(ItemRepository itemRepository, UserRepository userRepository, ShareTableRepository shareTableRepository, WorkspaceRepository workspaceRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.shareTableRepository = shareTableRepository;
        this.workspaceRepository = workspaceRepository;
    }

    public boolean canUserOn(User user, Item object, ShareTable.AccessType accessType, Long workspaceId) throws FileNotFoundException {
        if (object == null) {
            UserWorkspace workspace = workspaceRepository.getByUserId(user.getUserId());
            if (workspace == null) {
                return false;
            }

            return workspace.getWorkspaceId().equals(workspaceId);
        }

        Item parent;
        if (object.getItemType() == Item.ItemType.ITEM) {
            parent = itemRepository.loadItemById(object.getParentId());
        } else {
            parent = object;
        }

        if (parent.getOwnerId().equals(user.getUserId())) {
            return true;
        }

        if (user.getAccessLevel() == User.AccessLevel.FULL) {
            return true;
        }



        User owner = userRepository.getUserById(parent.getOwnerId());
        if (owner == null) {
            // wat. The constraint should stop this.
            throw new FileNotFoundException("Parent not found.");
        }
        List<ShareTable> shareTableList = shareTableRepository.getUserSharedFileWithUser(owner, user);
        if (shareTableList.isEmpty()) {
            return false;
        }

        for (ShareTable table : shareTableList) {
            Item rootParent = itemRepository.loadItemById(table.getItemId());
            if (rootParent == null) {
                continue;
            }

            if (itemRepository.isDescendantOf(rootParent, parent)) {
                if (accessType == ShareTable.AccessType.READ) {
                    return true;
                }

                return table.getAccessType() == ShareTable.AccessType.READ_WRITE;
            }
        }

        return false;
    }
}
