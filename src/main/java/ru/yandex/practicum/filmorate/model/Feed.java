package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.OperationType;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Getter
@Setter
@Builder
public class Feed {

    private Long eventId;
    private Long timestamp;
    @NotNull
    private Integer userId;
    @NotNull
    private EventType eventType;
    @NotNull
    private OperationType operation;
    @NotNull
    private Integer entityId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return eventId.equals(feed.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }
}
