package ru.yandex.practicum.filmorate.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.controller.UserController;
import com.google.gson.Gson;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    private UserController controller;
    private String tmpJson;
    private MockMvc mockMvc;
    private Gson gson;
    private User user;
    private User invalidEmail;
    private User invalidLogin;
    private User invalidBirthday;

    @Autowired
    UserControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    public void beforeEach() {
        gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).create();
        //валидеый юзер
        user = new User("mail@mail.ru", "dolore", "Nick Name", LocalDate.of(1992, 7, 7));
        //электронная почта не может быть пустой и должна содержать символ @;
        invalidEmail = new User("mailmail.ru", "dolore", "Nick Name", LocalDate.of(1992, 7, 7));
        //логин не может быть пустым и содержать пробелы;
        invalidLogin = new User("mail@mail.ru", "gg hh ii uu", "Nick Name", LocalDate.of(1992, 7, 7));
        //дата рождения не может быть в будущем.
        invalidBirthday = new User("mail@mail.ru", "dolore", "Nick Name", LocalDate.of(2992, 7, 7));
    }

    public void mockMvcPerformSuccessful(String tmpJson) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tmpJson))
                .andExpect(status().is2xxSuccessful());
    }

    public void mockMvcPerformError(String tmpJson) throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tmpJson))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void userValidationTest() throws Exception {
        tmpJson = gson.toJson(user);
        mockMvcPerformSuccessful(tmpJson);
        tmpJson = gson.toJson(invalidEmail);
        mockMvcPerformError(tmpJson);
        tmpJson = gson.toJson(invalidLogin);
        mockMvcPerformError(tmpJson);
        tmpJson = gson.toJson(invalidBirthday);
        mockMvcPerformError(tmpJson);
    }
}