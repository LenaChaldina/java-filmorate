package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

public interface FeedStorage {
    List<Feed> getFeedByUserId(int userId);

    void addFeedEvent(Feed build);
}
