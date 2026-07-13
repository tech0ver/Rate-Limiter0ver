package io.github.tech0ver.demo.annotation;

import io.github.tech0ver.demo.validator.ApiKeyValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ApiKeyValidator.class)
public @interface ValidApiKey {

    String message() default "Invalid API Key";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
