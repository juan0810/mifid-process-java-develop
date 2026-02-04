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
@Constraint(validatedBy = VersionValidator.class)
@Documented
public @interface EnsureVersion {
  String message() default "Version must be a positive number";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}