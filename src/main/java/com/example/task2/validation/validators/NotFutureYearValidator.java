package com.example.task2.validation.validators;

import com.example.task2.validation.constraints.NotFutureYear;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.Year;

public class NotFutureYearValidator implements ConstraintValidator<NotFutureYear, Integer> {

    @Override
    public void initialize(NotFutureYear constraintAnnotation) {

    }

    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {

        if (year == null) {
            return true;
        }

        int currentYear = Year.now().getValue();

        return year <= currentYear;
    }
}
