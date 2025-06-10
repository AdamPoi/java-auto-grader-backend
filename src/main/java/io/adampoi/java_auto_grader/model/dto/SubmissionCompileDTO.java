package io.adampoi.java_auto_grader.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SubmissionCompileDTO {
    private String code;

    private MultipartFile file;
}
