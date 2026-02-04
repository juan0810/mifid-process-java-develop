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
@Constraint(validatedBy = IdentificationClientValidator.class)
@Documented
public @interface EnsureIdentificationClient {
  String message() default "Client identification must be exactly 9 characters long";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}