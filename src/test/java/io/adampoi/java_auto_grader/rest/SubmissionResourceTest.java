package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.BulkSubmissionDTO;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.request.TestSubmitRequest;
import io.adampoi.java_auto_grader.model.type.CodeFile;
import io.adampoi.java_auto_grader.service.SubmissionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubmissionResourceTest {

    private static final String BASE_API_PATH = "/api/submissions";
    private static final String AUTHORITY_SUBMIT = "SUBMISSION:CREATE";
    private static final String AUTHORITY_BULK = "SUBMISSION:BULK_CREATE";
    private static final String AUTHORITY_TRYOUT = "SUBMISSION:TRYOUT";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SubmissionService submissionService;

    // ----- Mock helpers -----
    private CodeFile codeFile(String name, String content) {
        return CodeFile.builder().fileName(name).content(content).build();
    }

    private List<CodeFile> mockSourceFiles() {
        return List.of(codeFile("Main.java", "public class Main {}"));
    }

    private List<CodeFile> mockTestFiles() {
        return List.of(codeFile("MainTest.java", "public class MainTest {}"));
    }

    private UUID randomId() {
        return UUID.randomUUID();
    }

    @Nested
    @DisplayName("POST /api/submissions (submit by student)")
    class SubmitStudentSubmission {

        @Test
        @WithMockUser(authorities = {AUTHORITY_SUBMIT})
        @DisplayName("Should return 201 Created for valid submission")
        void submitStudent_ReturnsCreated() throws Exception {
            TestSubmitRequest request = TestSubmitRequest.builder()
                    .sourceFiles(mockSourceFiles())
                    .testFiles(mockTestFiles())
                    .mainClassName("Main")
                    .assignmentId(String.valueOf(UUID.randomUUID()))
                    .buildTool("gradle")
                    .build();

            SubmissionDTO responseDTO = SubmissionDTO.builder()
                    .id(randomId())
                    .assignmentId(UUID.fromString(request.getAssignmentId()))
                    .status("PASSED")
                    .build();

            when(submissionService.submitStudentSubmission(any(), any())).thenReturn(responseDTO);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.status").value("PASSED"));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void submitStudent_Unauthorized() throws Exception {
            TestSubmitRequest request = TestSubmitRequest.builder().build();

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_SUBMIT})
        @DisplayName("Should return 400 Bad Request for invalid input")
        void submitStudent_InvalidInput_ReturnsBadRequest() throws Exception {
            TestSubmitRequest request = TestSubmitRequest.builder().build();

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"));
        }
    }

    @Nested
    @DisplayName("POST /api/submissions/bulk (bulk upload)")
    class BulkUploadSubmission {

        @Test
        @WithMockUser(authorities = {AUTHORITY_BULK})
        @DisplayName("Should return 201 Created for bulk upload")
        void bulkUpload_ReturnsCreated() throws Exception {
            Map<String, List<CodeFile>> nimToCodeFiles = Map.of(
                    "2141720001", mockSourceFiles(),
                    "2141720002", mockSourceFiles()
            );
            BulkSubmissionDTO bulkResponse = BulkSubmissionDTO.builder()
                    .results(List.of(
                            BulkSubmissionDTO.Item.builder().nim("2141720001").success(true).message("OK").build(),
                            BulkSubmissionDTO.Item.builder().nim("2141720002").success(true).message("OK").build()
                    ))
                    .build();

            Map<String, Object> request = new HashMap<>();
            request.put("assignmentId", randomId());
            request.put("nimToCodeFiles", nimToCodeFiles);
            request.put("testFiles", mockTestFiles());
            request.put("mainClassName", "Main");
            request.put("buildTool", "gradle");

            when(submissionService.uploadBulkSubmission(any(), any(), any(), any(), any(), any()))
                    .thenReturn(bulkResponse);

            mockMvc.perform(post(BASE_API_PATH + "/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.results[0].success").value(true));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void bulkUpload_Unauthorized() throws Exception {
            Map<String, Object> request = new HashMap<>();
            request.put("assignmentId", randomId());
            request.put("nimToCodeFiles", Map.of("2141720001", mockSourceFiles()));
            request.put("testFiles", mockTestFiles());
            request.put("mainClassName", "Main");
            request.put("buildTool", "gradle");

            mockMvc.perform(post(BASE_API_PATH + "/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_BULK})
        @DisplayName("Should return 400 Bad Request for invalid input")
        void bulkUpload_InvalidInput_ReturnsBadRequest() throws Exception {
            Map<String, Object> request = new HashMap<>();

            mockMvc.perform(post(BASE_API_PATH + "/bulk")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"));
        }
    }

    @Nested
    @DisplayName("POST /api/submissions/tryout (tryout submission)")
    class TryoutSubmission {

        @Test
        @WithMockUser(authorities = {AUTHORITY_TRYOUT})
        @DisplayName("Should return 200 OK for valid tryout")
        void tryout_ReturnsOk() throws Exception {
            TestSubmitRequest request = TestSubmitRequest.builder()
                    .sourceFiles(mockSourceFiles())
                    .testFiles(mockTestFiles())
                    .mainClassName("Main")
                    .assignmentId(String.valueOf(UUID.randomUUID()))
                    .buildTool("gradle")
                    .build();

            SubmissionDTO responseDTO = SubmissionDTO.builder()
                    .id(randomId())
                    .assignmentId(UUID.fromString(request.getAssignmentId()))
                    .status("PASSED")
                    .build();

            when(submissionService.tryoutSubmission(request)).thenReturn(responseDTO);

            mockMvc.perform(post(BASE_API_PATH + "/tryout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.status").value("PASSED"));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void tryout_Unauthorized() throws Exception {
            TestSubmitRequest request = TestSubmitRequest.builder()
                    .sourceFiles(mockSourceFiles())
                    .testFiles(mockTestFiles())
                    .mainClassName("Main")
                    .assignmentId(String.valueOf(UUID.randomUUID()))
                    .buildTool("gradle")
                    .build();

            mockMvc.perform(post(BASE_API_PATH + "/tryout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_TRYOUT})
        @DisplayName("Should return 400 Bad Request for invalid input")
        void tryout_InvalidInput_ReturnsBadRequest() throws Exception {
            TestSubmitRequest request = TestSubmitRequest.builder()
                    .sourceFiles(mockSourceFiles())
                    .testFiles(mockTestFiles())
                    .mainClassName("Main")
                    .assignmentId(String.valueOf(UUID.randomUUID()))
                    .buildTool("gradle")
                    .build();

            mockMvc.perform(post(BASE_API_PATH + "/tryout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"));
        }
    }
}
