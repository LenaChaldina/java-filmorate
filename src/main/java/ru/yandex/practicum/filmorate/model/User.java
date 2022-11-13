package ru.yandex.practicum.filmorate.model;
//целочисленный идентификатор — id;
//электронная почта — email;
//логин пользователя — login;
//имя для отображения — name;
//дата рождения — birthday.

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    transient int id;
    @Email(message = "Невалидная почта")
    final String email;
    @NotBlank(message = "логин не может быть пустым")
    @Pattern(message = "логин не может содержать пробелы", regexp = "[^\\s]*")
    final String login;
    String name;
    @PastOrPresent(message = "дата рождения не может быть в будущем")
    final LocalDate birthday;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }
}

