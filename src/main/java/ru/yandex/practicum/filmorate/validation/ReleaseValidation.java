package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class ReleaseValidation implements ConstraintValidator<ConsistentDateParameters, LocalDate> {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate bound;

    @Override
    public void initialize(ConsistentDateParameters constraintAnnotation) {
        bound = LocalDate.parse(constraintAnnotation.value(), dtf);
    }

    @Override
    public boolean isValid(LocalDate release, ConstraintValidatorContext context) {
        if (release == null) {
            return true;
        }

        return release.isAfter(bound) || release.equals(bound);
    }
}
