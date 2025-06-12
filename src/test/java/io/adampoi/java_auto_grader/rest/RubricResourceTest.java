package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.RubricDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.RubricService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RubricResourceTest {

    private static final String BASE_API_PATH = "/api/rubrics";
    private static final String AUTHORITY_LIST = "RUBRIC:LIST";
    private static final String AUTHORITY_CREATE = "RUBRIC:CREATE";
    private static final String AUTHORITY_READ = "RUBRIC:READ";
    private static final String AUTHORITY_UPDATE = "RUBRIC:UPDATE";
    private static final String AUTHORITY_DELETE = "RUBRIC:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private RubricService rubricService;
    private UUID testRubricId;
    private RubricDTO testRubricDTO;

    @BeforeEach
    void setUp() {
        testRubricId = UUID.randomUUID();
        testRubricDTO = createRubricDTO("Test Rubric");
    }

    private RubricDTO createRubricDTO(String name) {
        RubricDTO dto = new RubricDTO();
        dto.setName(name);
        dto.setDescription("Description for " + name);
        dto.setMaxPoints(BigDecimal.TEN);
        dto.setDisplayOrder(1);
        dto.setIsActive(true);
        dto.setAssignment(UUID.randomUUID());
        return dto;
    }

    private RubricDTO createRubricDTOWithId(UUID id, String name) {
        RubricDTO dto = createRubricDTO(name);
        dto.setId(id);
        return dto;
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC:CREATE"})
    public void createRubric_ReturnsCreated() throws Exception {
        RubricDTO rubricDTO = new RubricDTO();
        rubricDTO.setName("New Rubric");
        rubricDTO.setDescription("Description");
        rubricDTO.setMaxPoints(BigDecimal.TEN);
        rubricDTO.setDisplayOrder(1);
        rubricDTO.setIsActive(true);
        rubricDTO.setAssignment(UUID.randomUUID());

        RubricDTO createdRubricDTO = new RubricDTO();
        createdRubricDTO.setId(UUID.randomUUID());
        createdRubricDTO.setName("New Rubric");
        createdRubricDTO.setDescription("Description");
        createdRubricDTO.setMaxPoints(BigDecimal.TEN);
        createdRubricDTO.setDisplayOrder(1);
        createdRubricDTO.setIsActive(true);
        createdRubricDTO.setAssignment(UUID.randomUUID());

        when(rubricService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdRubricDTO);

        mockMvc.perform(post("/api/rubrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("New Rubric"))
                .andExpect(jsonPath("$.data.description").value("Description"))
                .andExpect(jsonPath("$.data.maxPoints").value(10.0))
                .andExpect(jsonPath("$.data.displayOrder").value(1))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.assignment").exists());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC:READ"})
    public void getRubric_ReturnsOk() throws Exception {
        RubricDTO rubricDTO = new RubricDTO();
        rubricDTO.setId(UUID.randomUUID());
        rubricDTO.setName("Test Rubric");

        when(rubricService.get(rubricDTO.getId())).thenReturn(rubricDTO);

        mockMvc.perform(get("/api/rubrics/" + rubricDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(rubricDTO.getName()));
    }

    @Nested
    @DisplayName("GET /api/rubrics")
    class GetAllRubrics {

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 OK with rubrics")
        void getAllRubrics_ReturnsOk() throws Exception {
            List<RubricDTO> rubricDTOList = Collections.singletonList(
                    createRubricDTOWithId(testRubricId, "Test Rubric"));
            Page<RubricDTO> rubricDTOPage = new PageImpl<>(rubricDTOList);

            when(rubricService.findAll(any(), any())).thenReturn(PageResponse.from(rubricDTOPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").exists());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 and support pagination")
        void getAllRubrics_WithPagination_ReturnsOk() throws Exception {
            List<RubricDTO> rubricDTOList = Collections.singletonList(
                    createRubricDTOWithId(testRubricId, "Paged Rubric"));
            Page<RubricDTO> rubricDTOPage = new PageImpl<>(rubricDTOList);

            when(rubricService.findAll(any(), any())).thenReturn(PageResponse.from(rubricDTOPage));

            mockMvc.perform(get(BASE_API_PATH + "?page=0&size=10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].name").value("Paged Rubric"))
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
        void getAllRubrics_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/rubrics/{id}")
    class UpdateRubric {

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 200 OK with updated rubric data")
        void updateRubric_ReturnsOk() throws Exception {
            RubricDTO updatedDTO = createRubricDTOWithId(testRubricId, "Updated Rubric");
            updatedDTO.setDescription("Updated Description");
            updatedDTO.setMaxPoints(BigDecimal.valueOf(15.0));
            updatedDTO.setDisplayOrder(2);
            updatedDTO.setIsActive(false);

            when(rubricService.update(eq(testRubricId), any(RubricDTO.class)))
                    .thenReturn(updatedDTO);

            mockMvc.perform(patch(BASE_API_PATH + "/" + testRubricId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRubricDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(testRubricId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Updated Rubric"))
                    .andExpect(jsonPath("$.data.description").value("Updated Description"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 404 Not Found when rubric doesn't exist")
        void updateRubric_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Rubric not found"))
                    .when(rubricService)
                    .update(eq(testRubricId), any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + testRubricId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRubricDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void updateRubric_WithValidationError_ReturnsBadRequest() throws Exception {
            RubricDTO invalidDTO = new RubricDTO();
            invalidDTO.setName("");

            mockMvc.perform(patch(BASE_API_PATH + "/" + testRubricId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void updateRubric_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(patch(BASE_API_PATH + "/" + testRubricId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRubricDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/rubrics/{id}")
    class DeleteRubric {

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 204 No Content on successful deletion")
        void deleteRubric_ReturnsOk() throws Exception {
            doNothing().when(rubricService).delete(testRubricId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testRubricId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 404 Not Found when rubric doesn't exist")
        void deleteRubric_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Rubric not found"))
                    .when(rubricService).delete(testRubricId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testRubricId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void deleteRubric_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/" + testRubricId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
