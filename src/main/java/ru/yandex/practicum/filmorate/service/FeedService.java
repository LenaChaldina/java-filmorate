package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.enums.EventTypeEnum;
import ru.yandex.practicum.filmorate.enums.OperationTypeEnum;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

@Service
public class FeedService {
    private final FeedStorage feedStorage;

    @Autowired
    public FeedService(@Qualifier("FeedDbStorage") FeedStorage feedStorage) {
        this.feedStorage = feedStorage;
    }

    public List<Feed> getFeedByUserId(int userId){
        return feedStorage.getFeedByUserId(userId);
    }

    public void addFeedEvent(int userId, int entityId, EventTypeEnum eventType, OperationTypeEnum operationType) {
        feedStorage.addFeedEvent(userId, entityId, eventType, operationType);
    }
}
