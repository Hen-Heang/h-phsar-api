package com.henheang.hphsar.utils;

public final class PaginationUtils {

    private PaginationUtils() {
    }

    /** Ceiling-divides total by pageSize to get the number of pages, e.g. 21 items / 5 per page = 5 pages. */
    public static int totalPages(int total, int pageSize) {
        if (total % pageSize == 0) {
            return total / pageSize;
        }
        return (total / pageSize) + 1;
    }
}
