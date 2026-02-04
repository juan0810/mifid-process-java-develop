package com.singularbank.mifid.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TestIdValidator.class)
@Documented
public @interface EnsureTestId {
  String message() default "Test ID must be a positive number";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
