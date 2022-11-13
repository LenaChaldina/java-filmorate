package ru.yandex.practicum.filmorate.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Month;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDate, LocalDate> {
    public final void initialize(final ReleaseDate annotation) {
    }

    public final boolean isValid(final LocalDate value, final ConstraintValidatorContext context) {
        //дата релиза — не раньше 28 декабря 1895 года;
        LocalDate filmStartDate = LocalDate.of(1895, Month.DECEMBER, 28);
        return value.isAfter(filmStartDate);
    }
}