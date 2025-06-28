package io.adampoi.java_auto_grader.model.response;

import com.fasterxml.jackson.annotation.JsonView;
import io.adampoi.java_auto_grader.model.dto.Views;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PageResponse<T> {
    @JsonView(Views.External.class)
    private List<T> content;
    @JsonView(Views.External.class)
    private int page;
    @JsonView(Views.External.class)
    private int size;
    @JsonView(Views.External.class)
    private long totalElements;
    @JsonView(Views.External.class)
    private int totalPages;
    @JsonView(Views.External.class)
    private boolean hasNext;
    @JsonView(Views.External.class)
    private boolean hasPrevious;

    public static <T> PageResponse<T> from(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }
}