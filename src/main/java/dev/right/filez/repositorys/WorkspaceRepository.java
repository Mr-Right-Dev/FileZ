package dev.right.filez.repositorys;

import dev.right.filez.exceptions.DuplicatedData;
import dev.right.filez.model.UserWorkspace;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class WorkspaceRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<UserWorkspace> rowMapper = new RowMapper<UserWorkspace>() {
        @Override
        public UserWorkspace mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserWorkspace workspace = new UserWorkspace();

            workspace.setWorkspaceId(rs.getLong("workspaceId"));
            workspace.setUserId(rs.getLong("userId"));

            return workspace;
        }
    };

    @Autowired
    public WorkspaceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Nullable
    public UserWorkspace getByUserId(Long userId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM userWorkspace WHERE userId=? LIMIT 1;",
                    rowMapper,
                    userId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Nullable
    public UserWorkspace getByWorkspaceId(Long workspaceId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM userWorkspace WHERE workspaceId=? LIMIT 1;",
                    rowMapper,
                    workspaceId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Long getOrCreateWorkspaceForUser(Long userId) throws NullPointerException {
        UserWorkspace workspace = getByUserId(userId);
        if (workspace != null) {
            return workspace.getWorkspaceId();
        }

        return createWorkspaceForUser(userId);
    }

    public Long createWorkspaceForUser(Long userId) throws DuplicatedData, NullPointerException {
        if (getByUserId(userId) != null) {
            throw new DuplicatedData("User already have an workspace.");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO userWorkspace (userId) VALUES (?);", new String[]{"workspaceId"});
                    ps.setLong(1, userId);
                    return ps;
                },
                keyHolder
        );

        return keyHolder.getKey().longValue();
    }
}
