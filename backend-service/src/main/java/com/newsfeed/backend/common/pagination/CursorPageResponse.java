package com.newsfeed.backend.common.pagination;

import java.util.List;
import java.util.function.Function;

public record CursorPageResponse<T>(List<T> items, String nextCursor, boolean hasMore) {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 50;

    /** Clamps a client-supplied page size into a safe range. */
    public static int clampLimit(Integer requested) {
        if (requested == null || requested <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(requested, MAX_LIMIT);
    }

    /**
     * Builds a page from a result set fetched with {@code limit + 1} rows: the extra row (if
     * present) signals {@code hasMore} and is trimmed off rather than returned, and the cursor is
     * derived from the last row actually returned to the caller.
     */
    public static <R, T> CursorPageResponse<T> fromOverFetch(
            List<R> overFetched, int limit, Function<R, T> mapper, Function<R, Cursor> cursorOf) {
        boolean hasMore = overFetched.size() > limit;
        List<R> page = hasMore ? overFetched.subList(0, limit) : overFetched;
        String nextCursor = hasMore ? cursorOf.apply(page.get(page.size() - 1)).encode() : null;
        return new CursorPageResponse<>(page.stream().map(mapper).toList(), nextCursor, hasMore);
    }
}
