package dev.right.filez.repositorys;

import dev.right.filez.model.User;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserRepository {
    private JdbcTemplate template;

    public JdbcTemplate getTemplate() {
        return template;
    }
    public static RowMapper<User> rowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();

            user.setUserId(rs.getLong("userId"));
            user.setEmail(rs.getString("email"));
            user.setAuthProvider(User.AuthProvider.valueOf(rs.getString("authProvider")));
            user.setProfileImageId(rs.getLong("profileImageId"));
            user.setUsername(rs.getString("username"));
            user.setAccessLevel(User.AccessLevel.valueOf(rs.getString("accessLevel")));
            user.setFilesTotalSizeCap(rs.getLong("filesTotalSizeCap"));
            user.setPassword(rs.getString("password"));
            user.setLocked(rs.getBoolean("locked"));
            user.setExpireTime(rs.getTimestamp("expireTime"));

            return user;
        }
    };

    @Autowired
    public void setTemplate(JdbcTemplate template) {
        this.template = template;
    }

    public void save(User user) {
        template.update(
                "INSERT INTO user (email, authProvider, profileImageId, username, accessLevel, filesTotalSizeCap, password) VALUES (?, ?, ?, ?, ?, ?, ?)",
                user.getEmail(),
                user.getAuthProvider(),
                user.getProfileImageId(),
                user.getUsername(),
                user.getAccessLevel(),
                user.getFilesTotalSizeCap(),
                user.getPassword()
        );
    }

    @Nullable
    public User getUserByEmail(String email) {
        return template.queryForObject("SELECT * FROM user WHERE email=? LIMIT 1;", new Object[]{email}, rowMapper);
    }
}
