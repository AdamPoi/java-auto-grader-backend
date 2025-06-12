package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.AssignmentDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.AssignmentService;
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

import static io.adampoi.java_auto_grader.model.dto.AssignmentDTO.builder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssignmentResourceTest {

    private static final String BASE_API_PATH = "/api/assignments";
    private static final String AUTHORITY_LIST = "ASSIGNMENT:LIST";
    private static final String AUTHORITY_CREATE = "ASSIGNMENT:CREATE";
    private static final String AUTHORITY_READ = "ASSIGNMENT:READ";
    private static final String AUTHORITY_UPDATE = "ASSIGNMENT:UPDATE";
    private static final String AUTHORITY_DELETE = "ASSIGNMENT:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AssignmentService assignmentService;
    private UUID testAssignmentId;
    private AssignmentDTO testAssignmentDTO;

    @BeforeEach
    void setUp() {
        testAssignmentId = UUID.randomUUID();
        testAssignmentDTO = createAssignmentDTO("Test Assignment");
    }

    private AssignmentDTO createAssignmentDTO(String title) {
        return builder()
                .title(title)
                .description("Description for " + title)
                .dueDate(OffsetDateTime.now().plusDays(7))
                .isPublished(true)
                .maxAttempts(3)
                .course(UUID.randomUUID())
                .createdByTeacher(UUID.randomUUID())
                .build();
    }

    private AssignmentDTO createAssignmentDTOWithId(UUID id, String title) {
        return builder()
                .id(id)
                .title(title)
                .description("Description for " + title)
                .dueDate(OffsetDateTime.now().plusDays(7))
                .isPublished(true)
                .maxAttempts(3)
                .course(UUID.randomUUID())
                .createdByTeacher(UUID.randomUUID())
                .build();
    }

    @Nested
    @DisplayName("GET /api/assignments")
    class GetAllAssignments {

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 OK with assignments")
        void getAllAssignments_ReturnsOk() throws Exception {
            List<AssignmentDTO> assignmentDTOList = Collections.singletonList(
                    createAssignmentDTOWithId(testAssignmentId, "Test Assignment"));
            Page<AssignmentDTO> assignmentDTOPage = new PageImpl<>(assignmentDTOList);

            when(assignmentService.findAll(any(), any())).thenReturn(PageResponse.from(assignmentDTOPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").exists());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 and support pagination OK")
        void getAllAssignments_WithPagination_ReturnsOk() throws Exception {
            List<AssignmentDTO> assignmentDTOList = Collections.singletonList(
                    createAssignmentDTOWithId(testAssignmentId, "Paged Assignment"));
            Page<AssignmentDTO> assignmentDTOPage = new PageImpl<>(assignmentDTOList);

            when(assignmentService.findAll(any(), any())).thenReturn(PageResponse.from(assignmentDTOPage));

            mockMvc.perform(get(BASE_API_PATH + "?page=0&size=10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value("Paged Assignment"))
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
        void getAllAssignments_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/assignments")
    class CreateAssignment {

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 201 Created with assignment data")
        void createAssignment_ReturnsCreated() throws Exception {
            AssignmentDTO createdAssignmentDTO = createAssignmentDTOWithId(testAssignmentId,
                    "New Assignment");

            when(assignmentService.create(any(AssignmentDTO.class))).thenReturn(createdAssignmentDTO);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAssignmentDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(testAssignmentId.toString()))
                    .andExpect(jsonPath("$.data.title").value("New Assignment"))
                    .andExpect(jsonPath("$.data.description")
                            .value("Description for New Assignment"))
                    .andExpect(jsonPath("$.data.isPublished").value(true))
                    .andExpect(jsonPath("$.data.maxAttempts").value(3))
                    .andExpect(jsonPath("$.data.course").exists())
                    .andExpect(jsonPath("$.data.createdByTeacher").exists());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void createAssignment_WithValidationError_ReturnsBadRequest() throws Exception {
            AssignmentDTO invalidDTO = new AssignmentDTO();
            invalidDTO.setTitle("");

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
        void createAssignment_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAssignmentDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/assignments/{id}")
    class GetAssignmentById {

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 200 OK with assignment data")
        void getAssignment_ReturnsOk() throws Exception {
            when(assignmentService.get(testAssignmentId)).thenReturn(
                    createAssignmentDTOWithId(testAssignmentId, "Test Assignment"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testAssignmentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("Test Assignment"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 404 Not Found when assignment doesn't exist")
        void getAssignment_NotFound_ReturnsNotFound() throws Exception {
            when(assignmentService.get(testAssignmentId))
                    .thenThrow(new EntityNotFoundException("Assignment not found"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testAssignmentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void getAssignmentById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH + "/" + testAssignmentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/assignments/{id}")
    class UpdateAssignment {

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 200 OK with updated assignment data")
        void updateAssignment_ReturnsOk() throws Exception {
            AssignmentDTO updatedDTO = createAssignmentDTOWithId(testAssignmentId, "Updated Assignment");
            updatedDTO.setDescription("Updated Description");
            updatedDTO.setDueDate(OffsetDateTime.now().plusDays(14));
            updatedDTO.setIsPublished(false);
            updatedDTO.setMaxAttempts(5);

            when(assignmentService.update(eq(testAssignmentId), any(AssignmentDTO.class)))
                    .thenReturn(updatedDTO);

            mockMvc.perform(patch(BASE_API_PATH + "/" + testAssignmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAssignmentDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(testAssignmentId.toString()))
                    .andExpect(jsonPath("$.data.title").value("Updated Assignment"))
                    .andExpect(jsonPath("$.data.description").value("Updated Description"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 404 Not Found when assignment doesn't exist")
        void updateAssignment_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Assignment not found"))
                    .when(assignmentService)
                    .update(eq(testAssignmentId), any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + testAssignmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAssignmentDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void updateAssignment_WithValidationError_ReturnsBadRequest() throws Exception {
            AssignmentDTO invalidDTO = new AssignmentDTO();
            invalidDTO.setTitle("");

            mockMvc.perform(patch(BASE_API_PATH + "/" + testAssignmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void updateAssignment_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(patch(BASE_API_PATH + "/" + testAssignmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testAssignmentDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/assignments/{id}")
    class DeleteAssignment {

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 204 No Content on successful deletion")
        void deleteAssignment_ReturnsOk() throws Exception {
            doNothing().when(assignmentService).delete(testAssignmentId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testAssignmentId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 404 Not Found when assignment doesn't exist")
        void deleteAssignment_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Assignment not found"))
                    .when(assignmentService).delete(testAssignmentId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testAssignmentId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void deleteAssignment_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/" + testAssignmentId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
