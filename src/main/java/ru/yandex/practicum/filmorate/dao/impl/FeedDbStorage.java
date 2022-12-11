package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.OperationType;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository("FeedDbStorage")
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Map<EventType, Integer> eventToNumDictionary;
    private final Map<OperationType, Integer> operationToNumDictionary;
    private final Map<Integer, EventType> eventToEnumDictionary;
    private final Map<Integer, OperationType> operationToEnumDictionary;

    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        eventToNumDictionary = new HashMap<>();
        eventToNumDictionary.put(EventType.FRIEND, 1);
        eventToNumDictionary.put(EventType.LIKE, 2);
        eventToNumDictionary.put(EventType.REVIEW, 3);

        eventToEnumDictionary = new HashMap<>();
        eventToEnumDictionary.put(1, EventType.FRIEND);
        eventToEnumDictionary.put(2, EventType.LIKE);
        eventToEnumDictionary.put(3, EventType.REVIEW);

        operationToNumDictionary = new HashMap<>();
        operationToNumDictionary.put(OperationType.ADD, 1);
        operationToNumDictionary.put(OperationType.UPDATE, 2);
        operationToNumDictionary.put(OperationType.REMOVE, 3);

        operationToEnumDictionary = new HashMap<>();
        operationToEnumDictionary.put(1, OperationType.ADD);
        operationToEnumDictionary.put(2, OperationType.UPDATE);
        operationToEnumDictionary.put(3, OperationType.REMOVE);
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
    public void addFeedEvent(Feed feed) {
        SimpleJdbcInsert simpleJdbcInsertFilm = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("FEED_MODEL")
                .usingGeneratedKeyColumns("EVENT_ID");

        if(feed.getEventType().equals(EventType.REVIEW) && feed.getOperation().equals(OperationType.REMOVE)) {
            getUserIdForDeleteReviewEvent(feed);
        }

        final Map<String, Object> dataToInsert = new HashMap<>();
        dataToInsert.put("TIMESTAMP", Timestamp.valueOf(LocalDateTime.now()).getTime());
        dataToInsert.put("USER_ID", feed.getUserId());
        dataToInsert.put("EVENT_TYPE", eventToNumDictionary.get(feed.getEventType()));
        dataToInsert.put("OPERATION_TYPE", operationToNumDictionary.get(feed.getOperation()));
        dataToInsert.put("ENTITY_ID", feed.getEntityId());

        simpleJdbcInsertFilm.execute(dataToInsert);
    }

    private Feed makeFeed(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .eventId(rs.getLong("EVENT_ID"))
                .timestamp(rs.getLong("TIMESTAMP"))
                .userId(rs.getInt("USER_ID"))
                .eventType(eventToEnumDictionary.get(rs.getInt("EVENT_TYPE")))
                .operation(operationToEnumDictionary.get(rs.getInt("OPERATION_TYPE")))
                .entityId(rs.getInt("ENTITY_ID"))
                .build();
    }

    private void getUserIdForDeleteReviewEvent(Feed feed) {
        int userId = jdbcTemplate.queryForObject("SELECT USER_ID from reviews WHERE REVIEW_ID = ?", Integer.class, feed.getEntityId());
        feed.setUserId(userId);
    }
}
