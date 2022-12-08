//package ru.yandex.practicum.filmorate.controller;
//
//import com.google.gson.TypeAdapter;
//import com.google.gson.stream.JsonReader;
//import com.google.gson.stream.JsonWriter;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.time.LocalDate;
//
//@Component
//public class LocalDateAdapter extends TypeAdapter<LocalDate> {
//
//    @Override
//    public void write(final JsonWriter jsonWriter, final LocalDate localDate) throws IOException {
//        jsonWriter.value(localDate.toString());
//    }
//
//    @Override
//    public LocalDate read(final JsonReader jsonReader) throws IOException {
//        return LocalDate.parse(jsonReader.nextString());
//    }
//}
