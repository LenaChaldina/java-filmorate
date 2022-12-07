package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.OperationType;
import ru.yandex.practicum.filmorate.model.Feed;
import java.util.List;
import java.util.Map;

public interface FeedStorage {
    List<Feed> getFeedByUserId(int userId);

    void addFeedEvent(Map<String, Integer> parameters, EventType eventType, OperationType operation);
}
