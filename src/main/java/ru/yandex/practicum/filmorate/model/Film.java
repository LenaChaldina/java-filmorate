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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
    @NotNull
    Mpa mpa;
    SortedSet<Genre> genres = new TreeSet<>(Comparator.comparingInt(Genre::getId));
    List<Director> directors;

    public Film(String name, String description, LocalDate releaseDate, Long duration, Mpa mpa) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.mpa = mpa;
    }
}
