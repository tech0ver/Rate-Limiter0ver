package io.github.tech0ver.demo.validator;

import io.github.tech0ver.demo.annotation.ValidApiKey;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ApiKeyValidator implements ConstraintValidator<ValidApiKey, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;
        return value.matches("^t0-[A-Z]{3}-[0-9]{3}$");
    }

}
