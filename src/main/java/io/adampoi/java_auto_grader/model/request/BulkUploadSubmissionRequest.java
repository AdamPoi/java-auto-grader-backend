package io.adampoi.java_auto_grader.model.request;

import io.adampoi.java_auto_grader.model.type.CodeFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadSubmissionRequest {

    @NotNull(groups = CreateGroup.class)
    private UUID assignmentId;
    @NotEmpty(groups = CreateGroup.class)
    private Map<String, List<CodeFile>> nimToCodeFiles;
    @NotNull(groups = CreateGroup.class)
    private List<CodeFile> testFiles;
    @NotBlank(groups = CreateGroup.class)
    private String mainClassName;
    @NotBlank(groups = CreateGroup.class)
    private String buildTool;

    public interface CreateGroup {
    }
}
