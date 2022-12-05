package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import ru.yandex.practicum.filmorate.enums.EventTypeEnum;
import ru.yandex.practicum.filmorate.enums.OperationTypeEnum;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@Builder
public class Feed {
    @Id
    @NotNull
    Long eventId;
    Timestamp timestamp;
    @NotNull
    Integer userId;
    @NotNull
    EventTypeEnum eventType;
    @NotNull
    OperationTypeEnum operationType;
    @NotNull
    Integer entityId;
}
