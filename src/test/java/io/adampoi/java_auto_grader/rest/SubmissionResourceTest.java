package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.SubmissionService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.adampoi.java_auto_grader.model.dto.SubmissionDTO.builder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubmissionResourceTest {

    private static final String BASE_API_PATH = "/api/submissions";
    private static final String AUTHORITY_LIST = "SUBMISSION:LIST";
    private static final String AUTHORITY_CREATE = "SUBMISSION:CREATE";
    private static final String AUTHORITY_READ = "SUBMISSION:READ";
    private static final String AUTHORITY_UPDATE = "SUBMISSION:UPDATE";
    private static final String AUTHORITY_DELETE = "SUBMISSION:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private SubmissionService submissionService;

    private UUID testSubmissionId;
    private SubmissionDTO testSubmissionDTO;

    @BeforeEach
    void setUp() {
        testSubmissionId = UUID.randomUUID();
        testSubmissionDTO = createTestSubmissionDTO();
    }

    private SubmissionDTO createTestSubmissionDTO() {
        return builder()
                .id(testSubmissionId)
                .submissionTime(OffsetDateTime.now())
                .attemptNumber(1)
                .status("PENDING")
                .assignment(UUID.randomUUID())
                .student(UUID.randomUUID())
                .build();
    }

    @Nested
    @DisplayName("GET /api/submissions")
    class GetAllSubmissions {
        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("Should return 200 OK with submissions list")
        void getAllSubmissions_ReturnsOk() throws Exception {
            SubmissionDTO submission = builder()
                    .id(UUID.randomUUID())
                    .status("GRADED")
                    .build();

            List<SubmissionDTO> submissionList = Collections.singletonList(submission);
            Page<SubmissionDTO> submissionPage = new PageImpl<>(submissionList);

            when(submissionService.findAll(any(), any()))
                    .thenReturn(PageResponse.from(submissionPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].status").value(submission.getStatus()));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("Should return 200 OK with paginated submissions")
        void getAllSubmissions_WithPagination_ReturnsOk() throws Exception {
            SubmissionDTO submission = builder()
                    .id(UUID.randomUUID())
                    .status("PAGED_SUBMISSION")
                    .build();

            List<SubmissionDTO> submissionList = Collections.singletonList(submission);
            Page<SubmissionDTO> submissionPage = new PageImpl<>(submissionList);

            when(submissionService.findAll(any(), any()))
                    .thenReturn(PageResponse.from(submissionPage));

            mockMvc.perform(get(BASE_API_PATH + "?page=0&size=10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].status").value("PAGED_SUBMISSION"))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.hasPrevious").value(false));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void getAllSubmissions_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/submissions")
    class CreateSubmission {
        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("Should return 201 Created with new submission")
        void createSubmission_ReturnsCreated() throws Exception {
            SubmissionDTO request = builder()
                    .submissionTime(OffsetDateTime.now())
                    .attemptNumber(1)
                    .status("PENDING")
                    .assignment(UUID.randomUUID())
                    .student(UUID.randomUUID())
                    .build();

            SubmissionDTO createdSubmission = builder()
                    .id(UUID.randomUUID())
                    .submissionTime(request.getSubmissionTime())
                    .attemptNumber(request.getAttemptNumber())
                    .status(request.getStatus())
                    .assignment(request.getAssignment())
                    .student(request.getStudent())
                    .build();

            when(submissionService.create(any())).thenReturn(createdSubmission);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.attemptNumber")
                            .value(createdSubmission.getAttemptNumber()))
                    .andExpect(jsonPath("$.data.status").value(createdSubmission.getStatus()))
                    .andExpect(jsonPath("$.data.assignment").exists())
                    .andExpect(jsonPath("$.data.student").exists());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void createSubmission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            SubmissionDTO request = new SubmissionDTO();
            request.setSubmissionTime(OffsetDateTime.now());
            request.setAttemptNumber(1);
            request.setAssignment(UUID.randomUUID());
            request.setStudent(UUID.randomUUID());

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("Should return 400 Bad Request for invalid input")
        void createSubmission_WithValidationError_ReturnsBadRequest() throws Exception {
            SubmissionDTO request = new SubmissionDTO();
            request.setStatus(null);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/submissions/{id}")
    class GetSubmissionById {
        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("Should return 200 OK with submission details")
        void getSubmission_ReturnsOk() throws Exception {
            SubmissionDTO submission = builder()
                    .id(testSubmissionId)
                    .status("GRADED")
                    .build();

            when(submissionService.get(testSubmissionId)).thenReturn(submission);

            mockMvc.perform(get(BASE_API_PATH + "/" + testSubmissionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value(submission.getStatus()));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void getSubmissionById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH + "/" + testSubmissionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("Should return 404 Not Found for non-existent submission")
        void getSubmission_NotFound_ReturnsNotFound() throws Exception {
            when(submissionService.get(testSubmissionId))
                    .thenThrow(new EntityNotFoundException("Submission not found"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testSubmissionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/submissions/{id}")
    class UpdateSubmission {
        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("Should return 200 OK with updated submission")
        void updateSubmission_ReturnsOk() throws Exception {
            SubmissionDTO updateRequest = builder()
                    .status("COMPLETED")
                    .graderFeedback("Good job!")
                    .attemptNumber(2)
                    .build();

            SubmissionDTO updatedSubmission = builder()
                    .id(testSubmissionId)
                    .status(updateRequest.getStatus())
                    .graderFeedback(updateRequest.getGraderFeedback())
                    .attemptNumber(updateRequest.getAttemptNumber())
                    .assignment(UUID.randomUUID())
                    .student(UUID.randomUUID())
                    .build();

            when(submissionService.update(eq(testSubmissionId), any(SubmissionDTO.class)))
                    .thenReturn(updatedSubmission);

            mockMvc.perform(patch(BASE_API_PATH + "/" + testSubmissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(testSubmissionId.toString()))
                    .andExpect(jsonPath("$.data.status").value(updatedSubmission.getStatus()))
                    .andExpect(jsonPath("$.data.graderFeedback")
                            .value(updatedSubmission.getGraderFeedback()));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void updateSubmission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            SubmissionDTO updateRequest = new SubmissionDTO();
            updateRequest.setStatus("COMPLETED");
            updateRequest.setAttemptNumber(2);

            mockMvc.perform(patch(BASE_API_PATH + "/" + testSubmissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("Should return 404 Not Found for non-existent submission")
        void updateSubmission_NotFound_ReturnsNotFound() throws Exception {
            SubmissionDTO updateRequest = new SubmissionDTO();
            updateRequest.setStatus("FAILED");
            updateRequest.setAttemptNumber(2);

            doThrow(new EntityNotFoundException("Submission not found"))
                    .when(submissionService)
                    .update(eq(testSubmissionId), any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + testSubmissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("Should return 400 Bad Request for invalid input")
        void updateSubmission_WithValidationError_ReturnsBadRequest() throws Exception {
            SubmissionDTO updateRequest = new SubmissionDTO();

            mockMvc.perform(patch(BASE_API_PATH + "/" + testSubmissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }
    }

    @Nested
    @DisplayName("DELETE /api/submissions/{id}")
    class DeleteSubmission {
        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("Should return 204 No Content on successful deletion")
        void deleteSubmission_ReturnsOk() throws Exception {
            doNothing().when(submissionService).delete(testSubmissionId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testSubmissionId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void deleteSubmission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/" + testSubmissionId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("Should return 404 Not Found for non-existent submission")
        void deleteSubmission_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Submission not found"))
                    .when(submissionService).delete(testSubmissionId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testSubmissionId))
                    .andExpect(status().isNotFound());
        }
    }
}
