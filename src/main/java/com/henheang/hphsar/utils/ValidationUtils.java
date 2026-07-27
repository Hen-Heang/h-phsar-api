package com.henheang.hphsar.utils;

import com.henheang.hphsar.common.ExceptionMessages;
import com.henheang.hphsar.exception.BadRequestException;

public final class ValidationUtils {

    private static final int MAX_SAFE_INT = 2147483646;

    private ValidationUtils() {
    }

    /** Rejects any request-supplied Integer that exceeds the app's safe upper bound. Ignores nulls. */
    public static void rejectIfExceedsIntLimit(Integer... values) {
        for (Integer value : values) {
            if (value != null && value > MAX_SAFE_INT) {
                throw new BadRequestException(ExceptionMessages.INTEGER_OVERFLOW);
            }
        }
    }
}
