package ru.yandex.practicum.filmorate.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.enums.EventTypeEnum;
import ru.yandex.practicum.filmorate.enums.OperationTypeEnum;
import ru.yandex.practicum.filmorate.model.Feed;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Repository("FeedDbStorage")
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;
    private Map<EventTypeEnum, Integer> eventToNumDictionary;
    private Map<OperationTypeEnum, Integer> operationToNumDictionary;
    private Map<Integer, EventTypeEnum> eventToEnumDictionary;
    private Map<Integer, OperationTypeEnum> operationToEnumDictionary;



    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        eventToNumDictionary = new HashMap<>();
        eventToNumDictionary.put(EventTypeEnum.FRIEND, 1);
        eventToNumDictionary.put(EventTypeEnum.LIKE, 2);
        eventToNumDictionary.put(EventTypeEnum.REVIEW, 3);

        eventToEnumDictionary = new HashMap<>();
        eventToEnumDictionary.put(1, EventTypeEnum.FRIEND);
        eventToEnumDictionary.put(2, EventTypeEnum.LIKE);
        eventToEnumDictionary.put(3, EventTypeEnum.REVIEW);

        operationToNumDictionary = new HashMap<>();
        operationToNumDictionary.put(OperationTypeEnum.ADD, 1);
        operationToNumDictionary.put(OperationTypeEnum.UPDATE, 2);
        operationToNumDictionary.put(OperationTypeEnum.REMOVE, 3);

        operationToEnumDictionary = new HashMap<>();
        operationToEnumDictionary.put(1, OperationTypeEnum.ADD);
        operationToEnumDictionary.put(2, OperationTypeEnum.UPDATE);
        operationToEnumDictionary.put(3, OperationTypeEnum.REMOVE);
    }

    @Override
    public List<Feed> getFeedByUserId(int userId) {
        String sql = "SELECT EVENT_ID, TIMESTAMP, USER_ID, EVENT_TYPE, OPERATION_TYPE, ENTITY_ID " +
                "FROM FEED_MODEL " +
                "WHERE USER_ID = ?" +
                "ORDER BY TIMESTAMP DESC";
        return jdbcTemplate.query(sql, this::makeFeed, userId);
    }

    @Override
    public void addFeedEvent(int userId, int entityId, EventTypeEnum eventType, OperationTypeEnum operationType) {
        Optional<Integer> validationResult = ValidationEventData(userId, entityId, eventType);
        int validatedEntityId;
        if(validationResult.isEmpty()) {
            return;
        } else {
            validatedEntityId = validationResult.get();
        }

        SimpleJdbcInsert simpleJdbcInsertFilm = new SimpleJdbcInsert(this.jdbcTemplate)
                .withTableName("FEED_MODEL")
                .usingGeneratedKeyColumns("EVENT_ID");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("TIMESTAMP", Timestamp.valueOf(LocalDateTime.now()).toString());
        parameters.put("USER_ID", userId);
        parameters.put("EVENT_TYPE", eventToNumDictionary.get(eventType));
        parameters.put("OPERATION_TYPE", operationToNumDictionary.get(operationType));
        parameters.put("ENTITY_ID", validatedEntityId);

        simpleJdbcInsertFilm.executeAndReturnKey(parameters).longValue();
    }

    private Optional<Integer> ValidationEventData(int userId, int entityId, EventTypeEnum eventType) {
        if(eventType.equals(EventTypeEnum.LIKE)) {
            int count = jdbcTemplate.queryForObject("SELECT LIKE_ID from FILMS_LIKES WHERE USER_ID = ? and FILM_ID = ?", Integer.class, userId, entityId);
            return Optional.of(count);
        } else if(eventType.equals(EventTypeEnum.REVIEW)) {
            int count = jdbcTemplate.queryForObject("SELECT REVIEW_ID from reviews WHERE USER_ID = ? and REVIEW_ID = ?", Integer.class, userId, entityId);
            return Optional.of(count);
        } else if(eventType.equals(EventTypeEnum.FRIEND)) {
            int count = jdbcTemplate.queryForObject("SELECT FRIENDS_ID from USERS_FRIENDS WHERE USER_ID = ? and USER_FRIEND_ID = ?", Integer.class, userId, entityId);
            return Optional.of(count);
        } else {
            return Optional.empty();
        }
    }

    private Feed makeFeed(ResultSet rs, int rowNum) throws SQLException {
        return Feed.builder()
                .eventId(rs.getLong("EVENT_ID"))
                .timestamp(rs.getTimestamp("TIMESTAMP"))
                .userId(rs.getInt("USER_ID"))
                .eventType(eventToEnumDictionary.get(rs.getInt("EVENT_TYPE")))
                .operationType(operationToEnumDictionary.get(rs.getInt("OPERATION_TYPE")))
                .entityId(rs.getInt("ENTITY_ID"))
                .build();
    }
}
