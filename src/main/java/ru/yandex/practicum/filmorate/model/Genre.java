package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal=true)
public class Genre implements Comparable<Genre>{
    Integer id;
    String name;

    @Override
    public int compareTo(Genre o) {
        return Integer.compare(this.getId(), o.getId());
    }
}
