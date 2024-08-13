package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

import java.time.LocalDate;

@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
public class ReleaseValidation implements ConstraintValidator<ConsistentDateParameters, LocalDate> {
    LocalDate bound = LocalDate.of(1895, 12, 27);

    @Override
    public boolean isValid(LocalDate release, ConstraintValidatorContext constraintValidatorContext) {
        if (release == null) {
            return true;
        }
        return release.isAfter(bound);
    }
}
