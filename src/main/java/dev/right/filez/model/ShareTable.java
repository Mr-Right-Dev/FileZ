package dev.right.filez.model;

import jakarta.annotation.Nullable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class ShareTable {
    @Nullable
    private Long connectionId;

    private Long itemId;
    private Long itemOwnerId;
    private Long userId;
    private AccessType accessType;

    public enum AccessType {
        READ_WRITE,
        READ
    };

    @Nullable
    public Long getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(@Nullable Long connectionId) {
        this.connectionId = connectionId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getItemOwnerId() {
        return itemOwnerId;
    }

    public void setItemOwnerId(Long itemOwnerId) {
        this.itemOwnerId = itemOwnerId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }
}
