package io.adampoi.java_auto_grader.model.request;

import io.adampoi.java_auto_grader.model.type.CodeFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatCodeAnalyzerRequest {
    private List<CodeFile> studentCodes;
    private String instructions;
    private List<SimpleRubric> rubrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleRubric {
        private String name;
        private String description;
    }
}
