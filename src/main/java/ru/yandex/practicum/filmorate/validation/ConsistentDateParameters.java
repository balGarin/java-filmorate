package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = {ReleaseValidation.class})
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR,
        ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RUNTIME)
@Documented
public @interface ConsistentDateParameters {

    String message() default
            "Дата релиза не должна быть раньше  28 декабря 1895 года !";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}