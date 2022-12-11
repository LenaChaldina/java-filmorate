package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotBlank;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Director {
    Integer id;
    @NotBlank
    final String name;

    public Director(int id, String name) {
        this.id = id;
        this.name = name;
    }
}