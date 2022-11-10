package ru.yandex.practicum.filmorate.model;
//целочисленный идентификатор — id;
//название — name;
//описание — description;
//дата релиза — releaseDate;
//продолжительность фильма — duration.

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.validation.ReleaseDate;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Film {
    int id;
    @NotBlank(message = "название не может быть пустым")
    final String name;
    //максимальная длина описания — 200 символов;
    @Size(min = 1, max = 200, message = "максимальная длина описания — 200 символов")
    final String description;
    //дата релиза — не раньше 28 декабря 1895 года;
    @ReleaseDate
    final LocalDate releaseDate;
    @Positive(message = "продолжительность фильма должна быть положительной")
    final Long duration;

    public Film(String name, String description, LocalDate releaseDate, Long duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
