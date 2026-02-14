package dev.right.filez.storage;

import jakarta.annotation.Nullable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

    public enum AccessLevel {
        FULL, // can access all files, no restrictions and options from below
        USER_MANAGEMENT, // can create user but can't access users files
        NORMAL // can only share and manage own files
    }

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    public Long getFilesTotalSizeCap() {
        return filesTotalSizeCap;
    }

    public void setFilesTotalSizeCap(Long filesTotalSizeCap) {
        this.filesTotalSizeCap = filesTotalSizeCap;
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

    public Long getProfileImageId() {
        return profileImageId;
    }

    public void setProfileImageId(Long profileImageId) {
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
}
