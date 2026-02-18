CREATE TABLE user (
    userId BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    email VARCHAR(100) NOT NULL,
    authProvider ENUM('LOCAL', 'GOOGLE') NOT NULL,
    profileImageId BIGINT,
    username VARCHAR(30) NOT NULL,
    password VARCHAR(100),
    accessLevel ENUM('FULL', 'USER_MANAGEMENT', 'NORMAL') DEFAULT 'NORMAL' NOT NULL,
    filesTotalSizeCap BIGINT, -- null means no limit.
    creationTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    locked BOOLEAN DEFAULT FALSE,
    expireTime TIMESTAMP,
    accountCreator BIGINT DEFAULT -1,
    accumulatedFileSize BIGINT DEFAULT 0,

    UNIQUE (userId)
);

-- DEFAULT ADMIN CREDENTIALS
-- INSERT INTO user (username, email, authProvider, password, accessLevel) VALUES ('admin', 'super', 'LOCAL', '$2a$12$nb9iyDykIAkq31YAy37J1.NTz/UDghP3AHhT7pcLuFSBwwR2f03W.','FULL');
-- Email: super
-- Password @admin
-- Attention: Recommended to lock account after set up.

-- FILE SYSTEM

-- I'm not using references cuz
-- File clean up needs to be done by server.
-- (The path(the items) here is not related to the actual)

-- on 'parentId' when its null,
-- it's gonna be assumed that the parent is the workspace.
CREATE TABLE item (
    itemId BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    itemName VARCHAR(30),
    itemType ENUM('ITEM', 'FOLDER'),
    mimeType VARCHAR(30),
    ownerId BIGINT NOT NULL,
    size BIGINT,
    itemPath VARCHAR(100), -- NOTE only ITEM have an actual item path.
    creatingDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    parentId BIGINT,

    FOREIGN KEY (ownerId)
        REFERENCES user(userId),
    FOREIGN KEY (parentId)
        REFERENCES item(itemId),
    UNIQUE (itemId)
);

CREATE TABLE sharedTables (
    connectionId BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    itemId BIGINT NOT NULL,
    itemOwnerId BIGINT NOT NULL,
    userId BIGINT NOT NULL,
    accessType ENUM('READ_WRITE', 'READ') NOT NULL,

    FOREIGN KEY (userId)
        REFERENCES user(userId)
        ON DELETE CASCADE,
    FOREIGN KEY (itemOwnerId)
        REFERENCES user(userId)
        ON DELETE CASCADE,
    FOREIGN KEY (itemId)
        REFERENCES item(itemId)
        ON DELETE CASCADE,
    UNIQUE (connectionId)
);

-- 1 -> 1
-- 1 workspace per user
CREATE TABLE userWorkspace (
    workspaceId BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    userId BIGINT NOT NULL,

    UNIQUE (userId),
    FOREIGN KEY (userId)
        REFERENCES user(userId),
    UNIQUE (workspaceId)
);