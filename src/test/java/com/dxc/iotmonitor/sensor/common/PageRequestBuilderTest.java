package com.dxc.iotmonitor.sensor.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageRequestBuilderTest {

    @Test
    void from_withValidParams_returnsPageable() {
        var pageable = PageRequestBuilder.from(0, 20, "timestamp", "desc");

        assertNotNull(pageable);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
    }

    @Test
    void from_ascSortDir_returnsAscending() {
        var pageable = PageRequestBuilder.from(0, 10, "timestamp", "asc");

        assertNotNull(pageable);
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void from_sizeAboveMax_isClamped() {
        var pageable = PageRequestBuilder.from(0, 200, "timestamp", "desc");

        assertEquals(100, pageable.getPageSize());
    }

    @Test
    void withNullSortDir_defaultsToDescending() {
        var pageable = PageRequestBuilder.from(0, 10, "timestamp", null);

        assertNotNull(pageable);
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void from_negativePage_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PageRequestBuilder.from(-1, 10, "timestamp", "desc"));

        assertEquals("page must not be negative", ex.getMessage());
    }

    @Test
    void from_zeroSize_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PageRequestBuilder.from(0, 0, "timestamp", "desc"));

        assertEquals("size must be greater than 0", ex.getMessage());
    }

    @Test
    void from_nullSortBy_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PageRequestBuilder.from(0, 10, null, "desc"));

        assertEquals("sortBy is required", ex.getMessage());
    }

    @Test
    void from_blankSortBy_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> PageRequestBuilder.from(0, 10, "   ", "desc"));

        assertEquals("sortBy is required", ex.getMessage());
    }
}
