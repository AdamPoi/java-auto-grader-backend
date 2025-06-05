package io.adampoi.java_auto_grader.model.request;


import lombok.Data;

@Data
public class SearchRequest {
    private String query = "";
    private int page = 0;
    private int size = 10;
    private String sortBy = "id";
    private String sortDirection = "asc";
}


