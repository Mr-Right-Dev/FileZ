CREATE TABLE user (
    userId BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
    email VARCHAR(100) NOT NULL,
    authProvider ENUM('LOCAL', 'GOOGLE') NOT NULL,
    profileImageId BIGINT,
    username VARCHAR(30) NOT NULL,
    accessLevel ENUM('FULL', 'USER_MANAGEMENT', 'NORMAL') DEFAULT 'NORMAL' NOT NULL,
        filesTotalSizeCap BIGINT -- null means no limit.
);