package com.example.erp.service.pagination;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

@Service
public class PaginationService<T> {

    // calculates paginated items for the given page and page size
    public List<T> getPaginatedItems(List<T> items, int page, int pageSize) {
        int start = Math.max(0, (page - 1) * pageSize);
        int end = Math.min(start + pageSize, items.size());
        if (start >= items.size()) {
            return List.of();
        }
        return items.subList(start, end);
    }

    // calculates total number of pages
    public int getTotalPages(List<T> items, int pageSize) {
        if (pageSize <= 0) {
            return 1;
        }
        int totalPages = (int) Math.ceil((double) items.size() / pageSize);
        System.out.println("## TOTAL PAGE PAGINATION: " + totalPages);
        return totalPages;
    }

    // generates list of page numbers for display
    public List<Integer> getPageNumbers(int currentPage, int totalPages, int maxVisiblePages) {
        int start = Math.max(1, currentPage - maxVisiblePages / 2);
        int end = Math.min(start + maxVisiblePages - 1, totalPages);
        start = Math.max(1, end - maxVisiblePages + 1);
        
        return IntStream.rangeClosed(start, end)
                .boxed()
                .collect(Collectors.toList());
    }
}