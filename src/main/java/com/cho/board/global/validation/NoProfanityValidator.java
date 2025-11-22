package com.cho.board.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class NoProfanityValidator implements ConstraintValidator<NoProfanity, String> {

    private static final List<String> PROFANITY_LIST = List.of(
        "바보", "멍청이"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null || value.isBlank()) {
            return true;
        }

        return PROFANITY_LIST.stream()
            .noneMatch(word -> value.toLowerCase().contains(word));
    }
}
