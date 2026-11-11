package com.github.mcp.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {
    private List<T> items;
    private int page;
    private int perPage;
    private long totalCount;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
    private String nextPageUrl;
    private String prevPageUrl;

    public static <T> PagedResponse<T> of(List<T> items, int page, int perPage, long totalCount) {
        int totalPages = (int) Math.ceil((double) totalCount / perPage);
        return PagedResponse.<T>builder()
                .items(items)
                .page(page)
                .perPage(perPage)
                .totalCount(totalCount)
                .totalPages(totalPages)
                .hasNext(page < totalPages)
                .hasPrevious(page > 1)
                .build();
    }
}
