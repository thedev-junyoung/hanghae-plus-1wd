package io.hhplus.tdd.common.utils;

public class AssertUtil {
    public static void requirePositive(long value, String message) {
        if (value <= 0) throw new IllegalArgumentException(message);
    }
}
