package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.enums.EventTypeEnum;
import ru.yandex.practicum.filmorate.enums.OperationTypeEnum;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.Timestamp;
import java.util.List;

public interface FeedStorage {
    List<Feed> getFeedByUserId(int userId);

    void addFeedEvent(int userId, int entityId, EventTypeEnum eventType, OperationTypeEnum operationType);
}
