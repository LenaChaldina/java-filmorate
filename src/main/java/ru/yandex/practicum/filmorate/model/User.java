package ru.yandex.practicum.filmorate.model;
//целочисленный идентификатор — id;
//электронная почта — email;
//логин пользователя — login;
//имя для отображения — name;
//дата рождения — birthday.

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class User {
    private transient int id;
    @Email(message = "Невалидная почта")
    private final String email;
    @NotBlank(message = "логин не может быть пустым")
    @Pattern(message = "логин не может содержать пробелы", regexp = "[^\\s]*")
    private final String login;
    private String name;
    @PastOrPresent(message = "дата рождения не может быть в будущем")
    private final LocalDate birthday;

    public User(String email, String login, String name, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }
}

