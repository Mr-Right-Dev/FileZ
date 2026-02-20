package dev.right.filez.repositorys;

import dev.right.filez.model.Item;
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
import java.sql.Types;
import java.util.List;

@Repository
public class ItemRepository {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Item> rowMapper = new RowMapper<Item>() {
        @Override
        public Item mapRow(ResultSet rs, int rowNum) throws SQLException {
            Item item = new Item();

            item.setItemId(rs.getLong("itemId"));
            item.setItemName(rs.getString("itemName"));
            item.setItemType(Item.ItemType.valueOf(rs.getString("itemType")));
            item.setOwnerId(rs.getLong("ownerId"));
            item.setItemPath(rs.getString("itemPath"));
            item.setCreatingDate(rs.getTimestamp("creatingDate"));
            item.setMimeType(rs.getString("mimeType"));
            item.setSize(rs.getObject("size", Long.class));
            item.setParentId(rs.getObject("parentId", Long.class));
            item.setWorkspaceId(rs.getLong("workspaceId"));

            return item;
        }
    };

    @Autowired
    public ItemRepository(JdbcTemplate template) {
        this.jdbcTemplate = template;
    }

    @Nullable
    public Item loadItemById(Long itemId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM item WHERE itemId=?;",
                    rowMapper,
                    itemId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Nullable
    public Long save(Item item) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement("INSERT INTO item (itemName, itemType, ownerId, itemPath, mimeType, size, parentId, workspaceId) VALUES (?, ?, ?, ?, ?, ?, ?, ?);", new String[]{"itemId"});
                    ps.setString(1, item.getItemName());
                    ps.setString(2, item.getItemType().name());
                    ps.setLong(3, item.getOwnerId());
                    ps.setString(4, item.getItemPath());
                    ps.setString(5, item.getMimeType());
                    ps.setObject(6, item.getSize(), Types.BIGINT);
                    ps.setObject(7, item.getParentId(), Types.BIGINT);
                    ps.setLong(8, item.getWorkspaceId());
                    return ps;
                },
                keyHolder
        );

        try {
            return keyHolder.getKey().longValue();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void deleteItemById(Long itemId) {
        jdbcTemplate.update(
                "DELETE FROM item WHERE fileId=? LIMIT 1;", // Note: that 'LIMIT 1' is a hardcoded safeguard.
                itemId
        );
    }

    @Nullable
    public Item getItemByNameAndParentId(String itemName, @Nullable Long parentId, Long workspaceId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM item WHERE itemName=? AND parentId<=>? AND itemType=? AND workspaceId=?;",
                    rowMapper,
                    itemName,
                    parentId,
                    "FOLDER",
                    workspaceId
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Nullable
    public List<Item> getDescendants(Item item) {
        String sql = """
       
        WITH RECURSIVE descendants AS (
             SELECT *, 0 AS depth
             FROM item
             WHERE itemId = ?
         
             UNION ALL
         
             SELECT i.*, d.depth + 1
             FROM item i
             JOIN descendants d
                 ON i.parentId = d.itemId
             WHERE d.depth < 100
         )
         
         SELECT *
         FROM descendants;
                         
        """; // yeah pain.

        return jdbcTemplate.query(
                sql,
                rowMapper,
                item.getItemId()
        );
    }

    public boolean isDescendantOf(Item parent, Item child) {
        String sql = """
        
                WITH RECURSIVE descendants AS (
                
                    SELECT itemId, parentId
                    FROM item
                    WHERE itemId = ?
                
                    UNION ALL
                
                    SELECT i.itemId, i.parentId
                    FROM item i
                    JOIN descendants d
                        ON i.parentId = d.itemId
                )
                
                SELECT EXISTS(
                    SELECT 1
                    FROM descendants
                    WHERE itemId = ?
                );
        """;

        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(
                        sql,
                        Boolean.class,
                        parent.getItemId(),
                        child.getItemId()
                )
        );
    }
}
