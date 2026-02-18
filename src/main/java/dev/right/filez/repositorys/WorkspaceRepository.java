package dev.right.filez.repositorys;

import dev.right.filez.exceptions.DuplicatedData;
import dev.right.filez.model.UserWorkspace;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

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
            workspace.setOwnerId(rs.getLong("ownerId"));

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

    public void createWorkspaceForUser(Long userId) throws DuplicatedData {
        if (getByUserId(userId) != null) {
            throw new DuplicatedData("User already have an workspace.");
        }

        jdbcTemplate.update(
                "INSERT INTO userWorkspace (userId) VALUES (?);",
                userId
        );
    }
}
