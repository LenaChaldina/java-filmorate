package ru.yandex.practicum.filmorate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.controller.FilmController;
import com.google.gson.Gson;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {
    private FilmController controller;
    private String tmpJson;
    private final MockMvc mockMvc;
    private Gson gson;
    private Film film;
    private Film invalidName;
    private Film invalidSize;
    private Film invalidRelease;
    private Film invalidDuration;
    String desc = RandomStringUtils.random(201);

    @Autowired
    FilmControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    public void beforeEach() {
        gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
        //валидеый фильм
        film = new Film("nisi eiusmod", "adipisicing", LocalDate.of(1992, 7, 7), 100L, new Mpa(1, null));
        //название не может быть пустым;
        invalidName = new Film("", "adipisicing", LocalDate.of(1992, 7, 7), 100L, new Mpa(1, null));
        //максимальная длина описания — 200 символов;
        invalidSize = new Film("nisi eiusmod", desc, LocalDate.of(1992, 7, 7), 100L, new Mpa(1, null));
        //дата релиза — не раньше 28 декабря 1895 года;
        invalidRelease = new Film("nisi eiusmod", "adipisicing", LocalDate.of(1792, 7, 7), 100L, new Mpa(1, null));
        //продолжительность фильма должна быть положительной.
        invalidDuration = new Film("nisi eiusmod", "adipisicing", LocalDate.of(1992, 7, 7), -100L, new Mpa(1, null));
    }


    public void mockMvcPerformSuccessful(String tmpJson) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tmpJson))
                .andExpect(status().is2xxSuccessful());
    }

    public void mockMvcPerformError(String tmpJson) throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tmpJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void filmValidationTest() throws Exception {
        tmpJson = gson.toJson(film);
        mockMvcPerformSuccessful(tmpJson);
        tmpJson = gson.toJson(invalidName);
        mockMvcPerformError(tmpJson);
        tmpJson = gson.toJson(invalidSize);
        mockMvcPerformError(tmpJson);
        tmpJson = gson.toJson(invalidRelease);
        mockMvcPerformError(tmpJson);
        tmpJson = gson.toJson(invalidDuration);
        mockMvcPerformError(tmpJson);
    }
}
//package ru.yandex.practicum.filmorate.controller;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import com.google.gson.GsonBuilder;
//import org.apache.commons.lang3.RandomStringUtils;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.model.Mpa;
//import com.google.gson.Gson;
//
//import java.time.LocalDate;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class FilmControllerTest {
//    private FilmController controller;
//    private String tmpJson;
//    private final MockMvc mockMvc;
//    private Gson gson;
//    private Film film;
//    private Film invalidName;
//    private Film invalidSize;
//    private Film invalidRelease;
//    private Film invalidDuration;
//    String desc = RandomStringUtils.random(201);
//
//    @Autowired
//    FilmControllerTest(MockMvc mockMvc) {
//        this.mockMvc = mockMvc;
//    }
//
//    @BeforeEach
//    public void beforeEach() {
//        gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
//        //валидеый фильм
//        film = new Film("nisi eiusmod", "adipisicing", LocalDate.of(1992, 7, 7), 100L, new Mpa(1, null));
//        //название не может быть пустым;
//        invalidName = new Film("", "adipisicing", LocalDate.of(1992, 7, 7), 100L, new Mpa(1, null));
//        //максимальная длина описания — 200 символов;
//        invalidSize = new Film("nisi eiusmod", desc, LocalDate.of(1992, 7, 7), 100L, new Mpa(1, null));
//        //дата релиза — не раньше 28 декабря 1895 года;
//        invalidRelease = new Film("nisi eiusmod", "adipisicing", LocalDate.of(1792, 7, 7), 100L, new Mpa(1, null));
//        //продолжительность фильма должна быть положительной.
//        invalidDuration = new Film("nisi eiusmod", "adipisicing", LocalDate.of(1992, 7, 7), -100L, new Mpa(1, null));
//    }
//
//
//    public void mockMvcPerformSuccessful(String tmpJson) throws Exception {
//        mockMvc.perform(post("/films")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(tmpJson))
//                .andExpect(status().is2xxSuccessful());
//    }
//
//    public void mockMvcPerformError(String tmpJson) throws Exception {
//        mockMvc.perform(post("/films")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(tmpJson))
//                .andExpect(status().is4xxClientError());
//    }
//
//    @Test
//    public void filmValidationTest() throws Exception {
//        tmpJson = gson.toJson(film);
//        mockMvcPerformSuccessful(tmpJson);
//        tmpJson = gson.toJson(invalidName);
//        mockMvcPerformError(tmpJson);
//        tmpJson = gson.toJson(invalidSize);
//        mockMvcPerformError(tmpJson);
//        tmpJson = gson.toJson(invalidRelease);
//        mockMvcPerformError(tmpJson);
//        tmpJson = gson.toJson(invalidDuration);
//        mockMvcPerformError(tmpJson);
//    }
//}