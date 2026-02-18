package dev.right.filez.model;

import dev.right.filez.repositorys.UserRepository;
import dev.right.filez.services.UserService;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Optional;

@Component
@Scope("prototype")
public class User {
    private long userId;
    private String email;
    private AuthProvider authProvider;
    @Nullable
    private Long profileImageId;
    private String username;
    private AccessLevel accessLevel;
    @Nullable
    private Long filesTotalSizeCap;
    @Nullable
    private String password;
    private boolean locked;
    @Nullable
    private Timestamp expireTime;
    private Long accumulatedFileSize;

    public enum AccessLevel {
        FULL, // can access all files, no restrictions and options from below
        USER_MANAGEMENT, // can create user but can't access users files
        NORMAL // can only share and manage own files
    }

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    @Nullable
    public Timestamp getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(@Nullable Timestamp expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Nullable
    public Long getFilesTotalSizeCap() {
        return filesTotalSizeCap;
    }

    public void setFilesTotalSizeCap(@Nullable Long filesTotalSizeCap) {
        this.filesTotalSizeCap = filesTotalSizeCap;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public void setPassword(@Nullable String password) {
        this.password = password;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Nullable
    public Long getProfileImageId() {
        return profileImageId;
    }

    public void setProfileImageId(@Nullable Long profileImageId) {
        this.profileImageId = profileImageId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public Long getAccumulatedFileSize() {
        return accumulatedFileSize;
    }

    public void setAccumulatedFileSize(Long accumulatedFileSize) {
        this.accumulatedFileSize = accumulatedFileSize;
    }

    public Map<String, Object> toMap() {
        return Map.ofEntries(
                Map.entry("userId", this.userId),
                Map.entry("email", this.email),
                Map.entry("authProvider", this.authProvider),
                Map.entry("profileImageId", this.profileImageId != null ? this.profileImageId : -1),
                Map.entry("username", this.username),
                Map.entry("accessLevel", this.accessLevel),
                Map.entry("fileTotalSizeCap", this.filesTotalSizeCap != null ? this.filesTotalSizeCap : -1),
                Map.entry("locked", this.locked),
                Map.entry("expireTime", this.expireTime != null ? this.expireTime : -1),
                Map.entry("accumulatedFileSize", this.accumulatedFileSize)
        );
    }
}
