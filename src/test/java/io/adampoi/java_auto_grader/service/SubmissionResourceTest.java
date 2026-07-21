package io.adampoi.java_auto_grader.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.BulkSubmissionDTO;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.dto.TestExecutionDTO;
import io.adampoi.java_auto_grader.model.request.TestSubmitRequest;
import io.adampoi.java_auto_grader.model.type.CodeFile;
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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
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
    private static final String AUTHORITY_TRYOUT = "SUBMISSION:TEST";

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

    private TestExecutionDTO passedExecution(String methodName, UUID rubricGradeId) {
        return TestExecutionDTO.builder()
                .methodName(methodName)
                .status("PASSED")
                .executionTime(1L)
                .rubricGradeId(rubricGradeId)
                .rubricGrade(RubricGradeDTO.builder()
                        .id(rubricGradeId.toString())
                        .name(methodName.replace("()", ""))
                        .build())
                .build();
    }

    private String calculatorSolutionCode() {
        return """
                public class Calculator {
                    public static int add(int a, int b) {
                        return a + b;
                    }
                
                    public static int subtract(int a, int b) {
                        return a - b;
                    }
                
                    public static int multiply(int a, int b) {
                        return a * b;
                    }
                
                    public static double divide(int a, int b) {
                        return (double) a / b;
                    }
                
                    public static void main(String[] args) {
                        int a = 10;
                        int b = 4;
                
                        System.out.println("Sum: " + add(a, b));
                        System.out.println("Difference: " + subtract(a, b));
                        System.out.println("Product: " + multiply(a, b));
                        System.out.println("Quotient: " + divide(a, b));
                    }
                }
                """;
    }

    private String calculatorGeneratedTestCode() {
        return """
                package workspace;
                
                import org.junit.jupiter.api.Test;
                
                public class MainTest {
                    @Test
                    void testClassStructure() {
                    }
                
                    @Test
                    void testRequiredMembers() {
                    }
                
                    @Test
                    void testRequiredLogic() {
                    }
                }
                """;
    }

    @Nested
    @DisplayName("POST /api/submissions (submit by student)")
    class SubmitStudentSubmission {

        @Test
        @WithMockUser(authorities = {AUTHORITY_SUBMIT})
        @DisplayName("Should return 201 Created for valid submission")
        void submitStudent_ReturnsCreated() throws Exception {
            UUID studentId = randomId();
            TestSubmitRequest request = TestSubmitRequest.builder()
                    .sourceFiles(mockSourceFiles())
                    .testFiles(mockTestFiles())
                    .mainClassName("Main")
                    .userId(studentId.toString())
                    .assignmentId(String.valueOf(UUID.randomUUID()))
                    .buildTool("gradle")
                    .build();

            SubmissionDTO responseDTO = SubmissionDTO.builder()
                    .id(randomId())
                    .assignmentId(UUID.fromString(request.getAssignmentId()))
                    .status("PASSED")
                    .build();

            when(submissionService.submitStudentSubmission(eq(studentId), any())).thenReturn(responseDTO);

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
            TestSubmitRequest request = TestSubmitRequest.builder()
                    .sourceFiles(mockSourceFiles())
                    .testFiles(mockTestFiles())
                    .mainClassName("Main")
                    .userId(randomId().toString())
                    .assignmentId(randomId().toString())
                    .buildTool("gradle")
                    .build();

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

        @Test
        @WithMockUser(authorities = {AUTHORITY_SUBMIT})
        @DisplayName("Should submit assignment solution code and return all fulfilled rubric results")
        void submitStudent_WithAssignmentSolutionCode_ReturnsFullRubricResults() throws Exception {
            UUID assignmentId = randomId();
            UUID studentId = randomId();
            String solutionCode = calculatorSolutionCode();
            TestSubmitRequest request = TestSubmitRequest.builder()
                    .sourceFiles(List.of(codeFile("Calculator.java", solutionCode)))
                    .testFiles(List.of(codeFile("MainTest.java", calculatorGeneratedTestCode())))
                    .mainClassName("Calculator")
                    .assignmentId(assignmentId.toString())
                    .userId(studentId.toString())
                    .buildTool("gradle")
                    .build();

            UUID classStructureGradeId = randomId();
            UUID requiredMembersGradeId = randomId();
            UUID requiredLogicGradeId = randomId();
            SubmissionDTO responseDTO = SubmissionDTO.builder()
                    .id(randomId())
                    .assignmentId(assignmentId)
                    .studentId(studentId)
                    .status("COMPLETED")
                    .type("FINAL")
                    .totalPoints(100)
                    .testExecutions(List.of(
                            passedExecution("testClassStructure()", classStructureGradeId),
                            passedExecution("testRequiredMembers()", requiredMembersGradeId),
                            passedExecution("testRequiredLogic()", requiredLogicGradeId)
                    ))
                    .build();

            when(submissionService.submitStudentSubmission(eq(studentId), any(TestSubmitRequest.class)))
                    .thenReturn(responseDTO);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.assignmentId").value(assignmentId.toString()))
                    .andExpect(jsonPath("$.data.studentId").value(studentId.toString()))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.totalPoints").value(100))
                    .andExpect(jsonPath("$.data.testExecutions.length()").value(3))
                    .andExpect(jsonPath("$.data.testExecutions[*].status",
                            containsInAnyOrder("PASSED", "PASSED", "PASSED")))
                    .andExpect(jsonPath("$.data.testExecutions[*].methodName", containsInAnyOrder(
                            "testClassStructure()",
                            "testRequiredMembers()",
                            "testRequiredLogic()"
                    )))
                    .andExpect(jsonPath("$.data.testExecutions[*].rubricGradeId", containsInAnyOrder(
                            classStructureGradeId.toString(),
                            requiredMembersGradeId.toString(),
                            requiredLogicGradeId.toString()
                    )));

            verify(submissionService).submitStudentSubmission(eq(studentId), argThat(submissionRequest ->
                    submissionRequest.getAssignmentId().equals(assignmentId.toString())
                            && submissionRequest.getSourceFiles().size() == 1
                            && submissionRequest.getSourceFiles().get(0).getFileName().equals("Calculator.java")
                            && submissionRequest.getSourceFiles().get(0).getContent().equals(solutionCode)
                            && submissionRequest.getTestFiles().size() == 1
                            && submissionRequest.getTestFiles().get(0).getContent().contains("testRequiredLogic")
            ));
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
            request.put("teacherId", randomId().toString());
            request.put("assignmentId", randomId());
            request.put("nimToCodeFiles", nimToCodeFiles);
            request.put("testFiles", mockTestFiles());
            request.put("mainClassName", "Main");
            request.put("buildTool", "gradle");

            when(submissionService.uploadBulkSubmissionByNim(any(), any(), any(), any(), any(), any()))
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
            request.put("teacherId", randomId().toString());
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
            TestSubmitRequest request = TestSubmitRequest.builder().build();

            mockMvc.perform(post(BASE_API_PATH + "/tryout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"));
        }
    }
}
