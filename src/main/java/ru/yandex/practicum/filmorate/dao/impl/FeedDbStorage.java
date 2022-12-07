package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
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
    public void addFeedEvent(Map<String, Integer> parameters, EventType eventType, OperationType operation) {
        Map<String, Integer> data = ValidationEventData(parameters, eventType, operation);

        SimpleJdbcInsert simpleJdbcInsertFilm = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("FEED_MODEL")
                .usingGeneratedKeyColumns("EVENT_ID");

        final Map<String, Object> dataToInsert = new HashMap<>();
        dataToInsert.put("TIMESTAMP", Timestamp.valueOf(LocalDateTime.now()).getTime());
        dataToInsert.put("USER_ID", data.get("userId"));
        dataToInsert.put("EVENT_TYPE", eventToNumDictionary.get(eventType));
        dataToInsert.put("OPERATION_TYPE", operationToNumDictionary.get(operation));
        dataToInsert.put("ENTITY_ID", data.get("entityId"));

        simpleJdbcInsertFilm.execute(dataToInsert);
    }

    private Map<String, Integer> ValidationEventData(Map<String, Integer> parameters, EventType eventType, OperationType operation) {
        Map<String, Integer> result = new HashMap<>();
        SqlRowSet rowSet;
        if (eventType.equals(EventType.LIKE)) {
            rowSet = jdbcTemplate.queryForRowSet("SELECT USER_ID, FILM_ID from FILMS_LIKES WHERE USER_ID = ? and FILM_ID = ?", parameters.get("userId"), parameters.get("entityId"));
            if (rowSet.next()) {
                result.put("userId", rowSet.getInt("USER_ID"));
                result.put("entityId", rowSet.getInt("FILM_ID"));
            }
        } else if (eventType.equals(EventType.REVIEW)) {
            if (operation.equals(OperationType.REMOVE)) {
                rowSet = jdbcTemplate.queryForRowSet("SELECT USER_ID, REVIEW_ID from reviews WHERE REVIEW_ID = ?", parameters.get("entityId"));
            } else {
                rowSet = jdbcTemplate.queryForRowSet("SELECT USER_ID, REVIEW_ID from reviews WHERE USER_ID = ? and FILM_ID = ?", parameters.get("userId"), parameters.get("entityId"));
            }
            if (rowSet.next()) {
                result.put("userId", rowSet.getInt("USER_ID"));
                result.put("entityId", rowSet.getInt("REVIEW_ID"));
            }
        } else if (eventType.equals(EventType.FRIEND)) {
            rowSet = jdbcTemplate.queryForRowSet("SELECT USER_ID, USER_FRIEND_ID from USERS_FRIENDS WHERE USER_ID = ? and USER_FRIEND_ID = ?", parameters.get("userId"), parameters.get("entityId"));
            if (rowSet.next()) {
                result.put("userId", rowSet.getInt("USER_ID"));
                result.put("entityId", rowSet.getInt("USER_FRIEND_ID"));
            }
        }
        return result;
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
}
