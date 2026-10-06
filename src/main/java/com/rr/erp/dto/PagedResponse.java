package com.rr.erp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PagedResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PagedResponse() {
    }

    public PagedResponse(
            List<T> content,
            int page,
            int size,
            long totalElements
    ) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = size == 0
                ? 0
                : (int) Math.ceil((double) totalElements / size);
    }
}
