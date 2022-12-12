package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FeedStorage;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

@Slf4j
@Service
public class FeedService {
    private final FeedStorage feedStorage;

    @Autowired
    public FeedService(@Qualifier("FeedDbStorage") FeedStorage feedStorage) {
        this.feedStorage = feedStorage;
    }

    public List<Feed> getFeedByUserId(int userId){
        log.info(String.format("Запрошена лента новостей для пользователя %d",userId));
        return feedStorage.getFeedByUserId(userId);
    }
}
