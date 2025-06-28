package io.adampoi.java_auto_grader.model.request;

import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.type.CodeFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSubmissiionFeedback {

    private List<CodeFile> sourceFiles;

    private List<RubricGradeDTO> rubricGrades;

}
