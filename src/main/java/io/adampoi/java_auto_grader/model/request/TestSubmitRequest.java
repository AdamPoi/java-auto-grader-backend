package io.adampoi.java_auto_grader.model.request;

import io.adampoi.java_auto_grader.model.type.CodeFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmitRequest {

    @NotNull(groups = {CreateGroup.class, TryoutGroup.class})
    @Size(min = 1, message = "At least one source file must be provided", groups = {CreateGroup.class, TryoutGroup.class})
    private List<CodeFile> sourceFiles;
    @NotNull(groups = {CreateGroup.class, TryoutGroup.class})
    @Size(min = 1, message = "At least one test file must be provided", groups = {CreateGroup.class, TryoutGroup.class})
    private List<CodeFile> testFiles;
    @NotBlank(message = "Main class name must not be blank", groups = {CreateGroup.class, TryoutGroup.class})
    private String mainClassName;
    private String submissionId;
    @NotBlank(message = "User ID must not be blank", groups = CreateGroup.class)
    private String userId;
    @NotBlank(message = "Assignment ID must not be blank", groups = {CreateGroup.class, TryoutGroup.class})
    private String assignmentId;
    @NotBlank(message = "Build tool must be specified", groups = {CreateGroup.class, TryoutGroup.class})
    @Pattern(regexp = "gradle|maven", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Build Tool must be one of: gradle, maven", groups = {CreateGroup.class, TryoutGroup.class})
    private String buildTool;

    private String type;

    private boolean mutationTestingEnabled;

    public interface CreateGroup {
    }

    public interface TryoutGroup {
    }


}
