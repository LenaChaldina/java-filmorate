package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Genre {
    final int id;
    String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
