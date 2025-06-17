package io.adampoi.java_auto_grader.model.dto;

// com/example/json/Views.java
public class Views {
    // minimal view — for embedding in Classroom
    public interface External {
    }

    // full view extends Summary (so id+username are still included)
    public interface Internal extends External {
    }
}