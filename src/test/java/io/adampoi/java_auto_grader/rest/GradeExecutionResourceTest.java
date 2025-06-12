package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.GradeExecutionDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.GradeExecutionService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.adampoi.java_auto_grader.model.dto.GradeExecutionDTO.builder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GradeExecutionResourceTest {

    private static final String BASE_API_PATH = "/api/grade-executions";
    private static final String AUTHORITY_LIST = "GRADE_EXECUTION:LIST";
    private static final String AUTHORITY_CREATE = "GRADE_EXECUTION:CREATE";
    private static final String AUTHORITY_READ = "GRADE_EXECUTION:READ";
    private static final String AUTHORITY_UPDATE = "GRADE_EXECUTION:UPDATE";
    private static final String AUTHORITY_DELETE = "GRADE_EXECUTION:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private GradeExecutionService gradeExecutionService;
    private UUID testGradeExecutionId;
    private GradeExecutionDTO testGradeExecutionDTO;

    @BeforeEach
    void setUp() {
        testGradeExecutionId = UUID.randomUUID();
        testGradeExecutionDTO = createGradeExecutionDTO("PASSED", BigDecimal.TEN);
    }

    private GradeExecutionDTO createGradeExecutionDTO(String status, BigDecimal points) {
        return builder()
                .status(status)
                .pointsAwarded(points)
                .rubricGrade(UUID.randomUUID())
                .submission(UUID.randomUUID())
                .build();
    }

    private GradeExecutionDTO createGradeExecutionDTOWithId(UUID id, String status, BigDecimal points) {
        return builder()
                .id(id)
                .status(status)
                .pointsAwarded(points)
                .rubricGrade(UUID.randomUUID())
                .submission(UUID.randomUUID())
                .build();
    }

    @Nested
    @DisplayName("GET /api/grade-executions")
    class GetAllGradeExecutions {
        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 OK with grade executions")
        void shouldReturnGradeExecutionsWhenAuthorized() throws Exception {
            List<GradeExecutionDTO> gradeExecutionDTOList = Collections.singletonList(
                    createGradeExecutionDTOWithId(testGradeExecutionId, "PASSED", BigDecimal.TEN));
            Page<GradeExecutionDTO> gradeExecutionDTOPage = new PageImpl<>(gradeExecutionDTOList);

            when(gradeExecutionService.findAll(any(), any()))
                    .thenReturn(PageResponse.from(gradeExecutionDTOPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").exists());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 and support pagination")
        void shouldReturnPaginatedGradeExecutionsWhenRequested() throws Exception {
            List<GradeExecutionDTO> gradeExecutionDTOList = Collections.singletonList(
                    createGradeExecutionDTOWithId(testGradeExecutionId, "PAGINATED",
                            BigDecimal.valueOf(5)));
            Page<GradeExecutionDTO> gradeExecutionDTOPage = new PageImpl<>(gradeExecutionDTOList);

            when(gradeExecutionService.findAll(any(), any()))
                    .thenReturn(PageResponse.from(gradeExecutionDTOPage));

            mockMvc.perform(get(BASE_API_PATH + "?page=0&size=10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].status").value("PAGINATED"))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.size").value(1))
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.totalPages").value(1))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.hasPrevious").value(false));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void shouldReturnUnauthorizedWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/grade-executions")
    class CreateGradeExecution {
        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 201 Created with grade execution data")
        void createGradeExecution_ReturnsCreated() throws Exception {
            GradeExecutionDTO createdDTO = createGradeExecutionDTOWithId(testGradeExecutionId,
                    "PASSED", BigDecimal.TEN);

            when(gradeExecutionService.create(any(GradeExecutionDTO.class))).thenReturn(createdDTO);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testGradeExecutionDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(testGradeExecutionId.toString()))
                    .andExpect(jsonPath("$.data.status").value("PASSED"))
                    .andExpect(jsonPath("$.data.pointsAwarded").value(10.0))
                    .andExpect(jsonPath("$.data.rubricGrade").exists())
                    .andExpect(jsonPath("$.data.submission").exists());
            verify(gradeExecutionService, times(1)).create(any(GradeExecutionDTO.class));

        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void createGradeExecution_WithValidationError_ReturnsBadRequest() throws Exception {
            GradeExecutionDTO invalidDTO = builder()
                    .status(null)
                    .build();

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void createGradeExecution_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testGradeExecutionDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/grade-executions/{id}")
    class GetGradeExecutionById {
        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 200 OK with grade execution data")
        void getGradeExecution_ReturnsOk() throws Exception {
            when(gradeExecutionService.get(testGradeExecutionId)).thenReturn(
                    createGradeExecutionDTOWithId(testGradeExecutionId, "FAILED", BigDecimal.ZERO));


            mockMvc.perform(get(BASE_API_PATH + "/" + testGradeExecutionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("FAILED"));
            verify(gradeExecutionService, times(1)).get(testGradeExecutionId);
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 404 Not Found when grade execution doesn't exist")
        void getGradeExecution_NotFound_ReturnsNotFound() throws Exception {
            when(gradeExecutionService.get(testGradeExecutionId))
                    .thenThrow(new EntityNotFoundException("GradeExecution not found"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testGradeExecutionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void getGradeExecutionById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH + "/" + testGradeExecutionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/grade-executions/{id}")
    class UpdateGradeExecution {
        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 200 OK with updated grade execution data")
        void updateGradeExecution_ReturnsOk() throws Exception {
            GradeExecutionDTO updatedDTO = createGradeExecutionDTOWithId(testGradeExecutionId,
                    "ERROR", BigDecimal.ZERO);
            updatedDTO.setError("Test error");

            when(gradeExecutionService.update(eq(testGradeExecutionId), any(GradeExecutionDTO.class)))
                    .thenReturn(updatedDTO);


            mockMvc.perform(patch(BASE_API_PATH + "/" + testGradeExecutionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testGradeExecutionDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(testGradeExecutionId.toString()))
                    .andExpect(jsonPath("$.data.status").value("ERROR"))
                    .andExpect(jsonPath("$.data.error").value("Test error"));
            verify(gradeExecutionService, times(1)).update(eq(testGradeExecutionId),
                    any(GradeExecutionDTO.class));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 404 Not Found when grade execution doesn't exist")
        void updateGradeExecution_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("GradeExecution not found"))
                    .when(gradeExecutionService)
                    .update(eq(testGradeExecutionId), any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + testGradeExecutionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testGradeExecutionDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void updateGradeExecution_WithValidationError_ReturnsBadRequest() throws Exception {
            GradeExecutionDTO invalidDTO = builder()
                    .status("INVALID_STATUS")
                    .build();

            mockMvc.perform(patch(BASE_API_PATH + "/" + testGradeExecutionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void updateGradeExecution_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(patch(BASE_API_PATH + "/" + testGradeExecutionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testGradeExecutionDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/grade-executions/{id}")
    class DeleteGradeExecution {
        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 204 No Content on successful deletion")
        void deleteGradeExecution_ReturnsOk() throws Exception {
            doNothing().when(gradeExecutionService).delete(testGradeExecutionId);
            verify(gradeExecutionService, times(0)).delete(testGradeExecutionId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testGradeExecutionId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 404 Not Found when grade execution doesn't exist")
        void deleteGradeExecution_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("GradeExecution not found"))
                    .when(gradeExecutionService).delete(testGradeExecutionId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testGradeExecutionId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void deleteGradeExecution_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/" + testGradeExecutionId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
