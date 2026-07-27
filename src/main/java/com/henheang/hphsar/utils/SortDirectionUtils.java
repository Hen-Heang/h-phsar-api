package com.henheang.hphsar.utils;

import com.henheang.hphsar.common.ExceptionMessages;
import com.henheang.hphsar.exception.BadRequestException;

public final class SortDirectionUtils {

    private SortDirectionUtils() {
    }

    /** Rejects any sort direction other than "asc", "desc" (case-insensitive), or empty. */
    public static void validate(String sort) {
        if (!(sort.equalsIgnoreCase("asc") || sort.equalsIgnoreCase("desc") || sort.isEmpty())) {
            throw new BadRequestException(ExceptionMessages.FIELD_SORT_IS_INVALID_PLEASE_INPUT_EITHER_ASC);
        }
    }
}
