package com.dxc.iotmonitor.sensor.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageRequestBuilder {

    private static final int MAX_PAGE_SIZE = 100;

    private PageRequestBuilder() {
    }

    public static Pageable from(int page, int size, String sortBy, String sortDir) {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be greater than 0");
        }
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        if (sortBy == null || sortBy.isBlank()) {
            throw new IllegalArgumentException("sortBy is required");
        }
        boolean ascending = sortDir != null && sortDir.equalsIgnoreCase("asc");
        Sort sort = ascending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }
}
