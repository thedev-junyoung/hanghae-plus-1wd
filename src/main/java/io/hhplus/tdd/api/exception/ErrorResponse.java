package io.hhplus.tdd.api.exception;

public record ErrorResponse(
        String code,
        String message
) {
}
