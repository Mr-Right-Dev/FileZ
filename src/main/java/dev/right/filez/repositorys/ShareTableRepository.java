package dev.right.filez.repositorys;

import dev.right.filez.model.Item;
import dev.right.filez.model.ShareTable;
import dev.right.filez.model.User;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Repository
public class ShareTableRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<ShareTable> rowMapper = new RowMapper<ShareTable>() {
        @Override
        public ShareTable mapRow(ResultSet rs, int rowNum) throws SQLException {
            ShareTable shareTable = new ShareTable();

            shareTable.setConnectionId(rs.getLong("connectionId"));
            shareTable.setItemId(rs.getLong("itemId"));
            shareTable.setItemOwnerId(rs.getLong("itemOwnerId"));
            shareTable.setUserId(rs.getLong("userId"));
            shareTable.setAccessType(ShareTable.AccessType.valueOf(rs.getString("accessType")));

            return shareTable;
        }
    };

    @Autowired
    public ShareTableRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createNew(ShareTable shareTable) {
        jdbcTemplate.update(
                "INSERT INTO sharedTables (itemId, itemOwnerId, userId, accessType) VALUES (?,?,?,?);",
                shareTable.getItemId(),
                shareTable.getItemOwnerId(),
                shareTable.getUserId(),
                shareTable.getAccessType().name()
        );
    }

    @Nullable
    public ShareTable getShareTableOfFileForUser(Long itemId, User user) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM sharedTables WHERE itemId=? AND userId=?",
                    rowMapper,
                    itemId,
                    user.getUserId()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<ShareTable> getUserSharedFileWithUser(User owner, User target) {
        return jdbcTemplate.query(
                "SELECT * FROM sharedTables WHERE itemOwnerId=? AND userId=? LIMIT 1;",
                rowMapper,
                owner.getUserId(),
                target.getUserId()
        );
    }
}
