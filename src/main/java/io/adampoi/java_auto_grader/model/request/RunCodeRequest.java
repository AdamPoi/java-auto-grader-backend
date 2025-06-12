package io.adampoi.java_auto_grader.model.request;

import io.adampoi.java_auto_grader.model.dto.CodeFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeRequest {

    private List<CodeFile> files;
    private String mainClassName;

}
