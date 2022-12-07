package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.OperationType;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class Feed {
    @Id
    @NotNull
    Long eventId;
    Long timestamp;
    @NotNull
    Integer userId;
    @NotNull
    EventType eventType;
    @NotNull
    OperationType operation;
    @NotNull
    Integer entityId;
}
