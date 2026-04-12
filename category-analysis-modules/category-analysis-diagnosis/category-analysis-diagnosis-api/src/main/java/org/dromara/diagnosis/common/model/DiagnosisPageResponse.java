package org.dromara.diagnosis.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Unified page response for list APIs.
 */
public class DiagnosisPageResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long page;
    private long pageSize;
    private long total;
    private long totalPages;
    private List<T> items;

    public static <T> DiagnosisPageResponse<T> empty(long page, long pageSize) {
        DiagnosisPageResponse<T> response = new DiagnosisPageResponse<>();
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotal(0);
        response.setTotalPages(0);
        response.setItems(Collections.emptyList());
        return response;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
