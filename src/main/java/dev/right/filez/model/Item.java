package dev.right.filez.model;

import jakarta.annotation.Nullable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
@Scope("prototype")
public class Item {
    @Nullable
    private Long itemId;
    @Nullable
    private String itemName;
    private ItemType itemType;
    private Long ownerId;
    @Nullable
    private String itemPath;
    private Timestamp creatingDate;
    @Nullable
    private String mimeType;
    @Nullable
    private Long size;

    public enum ItemType {
        ITEM,
        FILE
    }

    @Nullable
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(@Nullable Long itemId) {
        this.itemId = itemId;
    }

    @Nullable
    public String getItemName() {
        return itemName;
    }

    public void setItemName(@Nullable String itemName) {
        this.itemName = itemName;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    @Nullable
    public String getItemPath() {
        return itemPath;
    }

    public void setItemPath(@Nullable String itemPath) {
        this.itemPath = itemPath;
    }

    public Timestamp getCreatingDate() {
        return creatingDate;
    }

    public void setCreatingDate(Timestamp creatingDate) {
        this.creatingDate = creatingDate;
    }

    @Nullable
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(@Nullable String mimeType) {
        this.mimeType = mimeType;
    }

    @Nullable
    public Long getSize() {
        return size;
    }

    public void setSize(@Nullable Long size) {
        this.size = size;
    }
}
