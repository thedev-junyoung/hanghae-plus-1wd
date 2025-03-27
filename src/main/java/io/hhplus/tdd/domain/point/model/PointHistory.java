package io.hhplus.tdd.domain.point.model;

public record PointHistory(
        long id,
        long userId,
        long amount,
        TransactionType type,
        long updateMillis
) {

}
