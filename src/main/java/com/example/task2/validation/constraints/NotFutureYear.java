package com.example.task2.validation.constraints;
import com.example.task2.validation.validators.NotFutureYearValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NotFutureYearValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotFutureYear {

    String message() default "The year cannot be later than the current year.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}