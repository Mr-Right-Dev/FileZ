package dev.right.filez.repositorys;

import dev.right.filez.model.Item;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
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
            item.setSize(rs.getLong("size"));

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

    public void save(Item item) {
        jdbcTemplate.update(
                "INSERT INTO item (itemName, itemType, ownerId, itemPath, mimeType, size) VALUES (?, ?, ?, ?, ?, ?);",
                item.getItemName(),
                item.getItemType().name(),
                item.getOwnerId(),
                item.getItemPath(),
                item.getMimeType(),
                item.getSize()
        );
    }

    public void deleteItemById(Long itemId) {
        jdbcTemplate.update(
                "DELETE FROM item WHERE fileId=? LIMIT 1;", // Note: that 'LIMIT 1' is a hardcoded safeguard.
                itemId
        );
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
                         )
                         
                         SELECT *
                         FROM descendants;
                         
        """; // yeah pain.

        try {
            return jdbcTemplate.query(
                    sql,
                    rowMapper,
                    item.getItemId()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public boolean isDescendantOf(Item parent, Item child) {
        String sql = """
        WITH RECURSIVE descendants AS (

            SELECT *
            FROM item
            WHERE itemId = ?

            UNION ALL

            SELECT i.*
            FROM item i
            JOIN descendants d
                ON i.parentId = d.itemId
        )

        SELECT EXISTS(
            SELECT 1
            FROM descendants
            WHERE itemId = ?
        )
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
