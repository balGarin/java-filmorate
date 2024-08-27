package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {ReleaseValidation.class})
@Target({ElementType.FIELD})
@Retention(RUNTIME)
@Documented
public @interface ConsistentDateParameters {

    String message() default
            "Дата релиза не должна быть раньше {value}";

    String value();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}