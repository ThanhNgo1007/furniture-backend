package com.furniture.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response DTO for cursor-based pagination.
 * Uses the last item's ID as cursor for efficient pagination.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorPageResponse<T> {
    
    private List<T> content;
    private Long nextCursor;  // ID of last item, null if no more data
    private boolean hasMore;
    private int size;
    private long totalElements;  // Optional: total count for display
    
    public static <T> CursorPageResponse<T> of(List<T> content, Long nextCursor, boolean hasMore, long totalElements) {
        CursorPageResponse<T> response = new CursorPageResponse<>();
        response.setContent(content);
        response.setNextCursor(nextCursor);
        response.setHasMore(hasMore);
        response.setSize(content.size());
        response.setTotalElements(totalElements);
        return response;
    }
    
    public static <T> CursorPageResponse<T> empty() {
        CursorPageResponse<T> response = new CursorPageResponse<>();
        response.setContent(List.of());
        response.setNextCursor(null);
        response.setHasMore(false);
        response.setSize(0);
        response.setTotalElements(0);
        return response;
    }
}
