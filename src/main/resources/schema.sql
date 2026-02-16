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
    locked BOOLEAN DEFUALT FALSE,
    expireTime TIMESTAMP
);

-- DEFAULT ADMIN CREDENTIALS
-- INSERT INTO user (username, email, authProvider, password, accessLevel) VALUES ('admin', 'super', 'LOCAL', '$2a$12$nb9iyDykIAkq31YAy37J1.NTz/UDghP3AHhT7pcLuFSBwwR2f03W.','FULL');
-- Email: super
-- Password @admin