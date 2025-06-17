package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.domain.RubricGrade;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.RubricGradeService;
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
@DisplayName("RubricGradeResourceTest")
class RubricGradeResourceTest {

    private static final String BASE_API_PATH = "/api/rubric-grades";
    private static final String AUTHORITY_LIST = "RUBRIC_GRADE:LIST";
    private static final String AUTHORITY_CREATE = "RUBRIC_GRADE:CREATE";
    private static final String AUTHORITY_READ = "RUBRIC_GRADE:READ";
    private static final String AUTHORITY_UPDATE = "RUBRIC_GRADE:UPDATE";
    private static final String AUTHORITY_DELETE = "RUBRIC_GRADE:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private RubricGradeService rubricGradeService;
    private UUID testRubricGradeId;
    private RubricGradeDTO testRubricGradeDTO;

    @BeforeEach
    void setUp() {
        testRubricGradeId = UUID.randomUUID();
        testRubricGradeDTO = createRubricGradeDTO("Test Rubric Grade");
    }

    private RubricGradeDTO createRubricGradeDTO(String name) {
        return RubricGradeDTO.builder()
                .name(name)
                .description("Description for " + name)
                .points(BigDecimal.valueOf(10.0))
                .displayOrder(1)
                .gradeType(RubricGrade.GradeType.FUNCTIONALITY)
                .rubric(UUID.randomUUID())
                .build();
    }

    private RubricGradeDTO createRubricGradeDTOWithId(UUID id, String name) {
        return RubricGradeDTO.builder()
                .id(id)
                .name(name)
                .description("Description for " + name)
                .points(BigDecimal.valueOf(10.0))
                .displayOrder(1)
                .gradeType(RubricGrade.GradeType.FUNCTIONALITY)
                .rubric(UUID.randomUUID())
                .build();
    }

    @Nested
    @DisplayName("GET /api/rubric-grades")
    class GetAllRubricGrades {

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 OK with rubric grades")
        void getAllRubricGrades_ReturnsOk() throws Exception {
            List<RubricGradeDTO> rubricGradeDTOList = Collections.singletonList(
                    createRubricGradeDTOWithId(testRubricGradeId, "Test Rubric Grade"));
            Page<RubricGradeDTO> rubricGradeDTOPage = new PageImpl<>(rubricGradeDTOList);

            when(rubricGradeService.findAll(any(), any())).thenReturn(PageResponse.from(rubricGradeDTOPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").exists());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 and support pagination")
        void getAllRubricGrades_WithPagination_ReturnsOk() throws Exception {
            List<RubricGradeDTO> rubricGradeDTOList = Collections.singletonList(
                    createRubricGradeDTOWithId(testRubricGradeId, "Paged Rubric Grade"));
            Page<RubricGradeDTO> rubricGradeDTOPage = new PageImpl<>(rubricGradeDTOList);

            when(rubricGradeService.findAll(any(), any())).thenReturn(PageResponse.from(rubricGradeDTOPage));

            mockMvc.perform(get(BASE_API_PATH + "?page=0&size=10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].name").value("Paged Rubric Grade"))
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
        void getAllRubricGrades_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/rubric-grades")
    class CreateRubricGrade {

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 201 Created with rubric grade data")
        void createRubricGrade_ReturnsCreated() throws Exception {
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("New Rubric Grade")
                    .description("Description")
                    .points(BigDecimal.valueOf(10.0))
                    .displayOrder(1)
                    .gradeType(RubricGrade.GradeType.FUNCTIONALITY)
                    .rubric(UUID.randomUUID())
                    .build();

            RubricGradeDTO createdRubricGradeDTO = RubricGradeDTO.builder()
                    .id(UUID.randomUUID())
                    .name("New Rubric Grade")
                    .description("Description")
                    .points(BigDecimal.valueOf(10.0))
                    .displayOrder(1)
                    .gradeType(RubricGrade.GradeType.FUNCTIONALITY)
                    .rubric(UUID.randomUUID())
                    .build();

            when(rubricGradeService.create(any())).thenReturn(createdRubricGradeDTO);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.name").value("New Rubric Grade"))
                    .andExpect(jsonPath("$.data.description").value("Description"))
                    .andExpect(jsonPath("$.data.points").value(10.0))
                    .andExpect(jsonPath("$.data.displayOrder").value(1))
                    .andExpect(jsonPath("$.data.code").value("CODE123"))
                    .andExpect(jsonPath("$.data.gradeType").value("FUNCTIONALITY"))
                    .andExpect(jsonPath("$.data.rubric").exists());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void createRubricGrade_WithValidationError_ReturnsBadRequest() throws Exception {
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("")
                    .build();

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void createRubricGrade_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("New Rubric Grade")
                    .build();

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/rubric-grades/{id}")
    class GetRubricGradeById {

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 200 OK with rubric grade data")
        void getRubricGrade_ReturnsOk() throws Exception {
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .id(UUID.randomUUID())
                    .name("Test Rubric Grade")
                    .build();

            when(rubricGradeService.get(rubricGradeDTO.getId())).thenReturn(rubricGradeDTO);

            mockMvc.perform(get(BASE_API_PATH + "/" + rubricGradeDTO.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value(rubricGradeDTO.getName()));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 404 Not Found when rubric grade doesn't exist")
        void getRubricGrade_NotFound_ReturnsNotFound() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            when(rubricGradeService.get(rubricGradeId))
                    .thenThrow(new EntityNotFoundException("RubricGrade not found"));

            mockMvc.perform(get(BASE_API_PATH + "/" + rubricGradeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void getRubricGradeById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            mockMvc.perform(get(BASE_API_PATH + "/" + rubricGradeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/rubric-grades/{id}")
    class UpdateRubricGrade {

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 200 OK with updated rubric grade data")
        void updateRubricGrade_ReturnsOk() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("Updated Rubric Grade")
                    .description("Updated Description")
                    .build();

            RubricGradeDTO updatedRubricGradeDTO = RubricGradeDTO.builder()
                    .id(rubricGradeId)
                    .name("Updated Rubric Grade")
                    .description("Updated Description")
                    .points(BigDecimal.valueOf(15.0))
                    .displayOrder(2)
                    .gradeType(RubricGrade.GradeType.COMPILATION)
                    .rubric(UUID.randomUUID())
                    .build();

            when(rubricGradeService.update(eq(rubricGradeId), any(RubricGradeDTO.class)))
                    .thenReturn(updatedRubricGradeDTO);

            mockMvc.perform(patch(BASE_API_PATH + "/" + rubricGradeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(rubricGradeId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Updated Rubric Grade"))
                    .andExpect(jsonPath("$.data.description").value("Updated Description"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 404 Not Found when rubric grade doesn't exist")
        void updateRubricGrade_NotFound_ReturnsNotFound() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
            rubricGradeDTO.setName("Updated Rubric Grade");

            doThrow(new EntityNotFoundException("RubricGrade not found"))
                    .when(rubricGradeService)
                    .update(eq(rubricGradeId), any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + rubricGradeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void updateRubricGrade_WithValidationError_ReturnsBadRequest() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("")
                    .build();

            mockMvc.perform(patch(BASE_API_PATH + "/" + rubricGradeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void updateRubricGrade_WithNoAuthority_ReturnsNotFound() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("Updated Rubric Grade")
                    .build();

            doThrow(new EntityNotFoundException("RubricGrade not found"))
                    .when(rubricGradeService)
                    .update(org.mockito.ArgumentMatchers.eq(rubricGradeId),
                            org.mockito.ArgumentMatchers.any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + rubricGradeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/rubric-grades/{id}")
    class DeleteRubricGrade {

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 204 No Content on successful deletion")
        void deleteRubricGrade_ReturnsOk() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            doNothing().when(rubricGradeService).delete(rubricGradeId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + rubricGradeId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 404 Not Found when rubric grade doesn't exist")
        void deleteRubricGrade_NotFound_ReturnsNotFound() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            doThrow(new EntityNotFoundException("RubricGrade not found")).when(rubricGradeService)
                    .delete(rubricGradeId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + rubricGradeId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void deleteRubricGrade_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();

            mockMvc.perform(delete(BASE_API_PATH + "/" + rubricGradeId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/rubric-grades - With No Authority")
    class GetAllRubricGrades_WithNoAuthority {
        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void getAllRubricGrades_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/rubric-grades")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/rubric-grades/{id} - With No Authority")
    class GetRubricGradeById_WithNoAuthority {
        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void getRubricGradeById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            mockMvc.perform(get("/api/rubric-grades/" + rubricGradeId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/rubric-grades - With No Authority")
    class CreateRubricGrade_WithNoAuthority {
        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void createRubricGrade_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("New Rubric Grade")
                    .build();

            mockMvc.perform(post("/api/rubric-grades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/rubric-grades/{id} - With No Authority")
    class UpdateRubricGrade_WithNoAuthority {
        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void updateRubricGrade_WithNoAuthority_ReturnsNotFound() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("Updated Rubric Grade")
                    .build();

            doThrow(new EntityNotFoundException("RubricGrade not found"))
                    .when(rubricGradeService)
                    .update(org.mockito.ArgumentMatchers.eq(rubricGradeId),
                            org.mockito.ArgumentMatchers.any());

            mockMvc.perform(patch("/api/rubric-grades/" + rubricGradeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/rubric-grades/{id} - With No Authority")
    class DeleteRubricGrade_WithNoAuthority {
        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void deleteRubricGrade_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();

            mockMvc.perform(delete("/api/rubric-grades/" + rubricGradeId))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/rubric-grades - With Validation Error")
    class CreateRubricGrade_WithValidationError {
        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void createRubricGrade_WithValidationError_ReturnsBadRequest() throws Exception {
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("")
                    .build();

            mockMvc.perform(post("/api/rubric-grades")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/rubric-grades/{id} - With Validation Error")
    class UpdateRubricGrade_WithValidationError {
        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void updateRubricGrade_WithValidationError_ReturnsBadRequest() throws Exception {
            UUID rubricGradeId = UUID.randomUUID();
            RubricGradeDTO rubricGradeDTO = RubricGradeDTO.builder()
                    .name("")
                    .build();

            mockMvc.perform(patch("/api/rubric-grades/" + rubricGradeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }
    }
}
