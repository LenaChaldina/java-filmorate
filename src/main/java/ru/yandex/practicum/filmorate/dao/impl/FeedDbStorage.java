package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.OperationType;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository("FeedDbStorage")
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Feed> getFeedByUserId(int userId) {
        String sql = "SELECT EVENT_ID, TIMESTAMP, USER_ID, EVENT_TYPE, OPERATION_TYPE, ENTITY_ID " +
                "FROM FEED_MODEL " +
                "WHERE USER_ID = ? " +
                "ORDER BY TIMESTAMP";
        return jdbcTemplate.query(sql, this::makeFeed, userId);
    }

    @Override
    public int addFeedEvent(Feed feed) {
        String sqlQuery = "INSERT INTO FEED_MODEL(TIMESTAMP, USER_ID, EVENT_TYPE, OPERATION_TYPE, ENTITY_ID) " +
                          "SELECT ?, ?, ED.EVENT_ID, OD.OPERATION_ID, ? " +
                          "FROM EVENT_DICTIONARY ED " +
                          "JOIN OPERATION_DICTIONARY OD " +
                          "WHERE ED.EVENT_NAME = ? " +
                          "AND OD.OPERATION_NAME = ?";

        if(feed.getEventType().equals(EventType.REVIEW) && feed.getOperation().equals(OperationType.REMOVE)) {
            getUserIdForDeleteReviewEvent(feed);
        }

        return jdbcTemplate.update(sqlQuery,
                Timestamp.valueOf(LocalDateTime.now()).getTime(),
                feed.getUserId(),
                feed.getEntityId(),
                feed.getEventType().toString(),
                feed.getOperation().toString());
    }

    private Feed makeFeed(ResultSet rs, int rowNum) throws SQLException {
        SqlRowSet rowSet = jdbcTemplate.queryForRowSet(
                "SELECT " +
                "ED.EVENT_NAME, OD.OPERATION_NAME " +
                "FROM EVENT_DICTIONARY ED " +
                "JOIN OPERATION_DICTIONARY OD " +
                "ON ED.EVENT_ID = ? AND OD.OPERATION_ID = ?", rs.getInt("EVENT_TYPE"), rs.getInt("OPERATION_TYPE"));
        rowSet.next();
        return Feed.builder()
                .eventId(rs.getLong("EVENT_ID"))
                .timestamp(rs.getLong("TIMESTAMP"))
                .userId(rs.getInt("USER_ID"))
                .eventType(EventType.valueOf(rowSet.getString("EVENT_NAME")))
                .operation(OperationType.valueOf(rowSet.getString("OPERATION_NAME")))
                .entityId(rs.getInt("ENTITY_ID"))
                .build();
    }

    private void getUserIdForDeleteReviewEvent(Feed feed) {
        int userId = jdbcTemplate.queryForObject("SELECT USER_ID from reviews WHERE REVIEW_ID = ?", Integer.class, feed.getEntityId());
        feed.setUserId(userId);
    }
}
