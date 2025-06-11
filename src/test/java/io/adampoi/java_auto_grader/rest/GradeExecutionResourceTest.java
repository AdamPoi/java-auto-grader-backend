package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.domain.GradeExecution;
import io.adampoi.java_auto_grader.model.dto.GradeExecutionDTO;
import io.adampoi.java_auto_grader.service.GradeExecutionService;
import jakarta.persistence.EntityNotFoundException;
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
public class GradeExecutionResourceTest {

    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private GradeExecutionService gradeExecutionService;

    @Test
    @WithMockUser(authorities = {"GRADE_EXECUTION:LIST"})
    public void getAllGradeExecutions_ReturnsOk() throws Exception {
        GradeExecutionDTO gradeExecutionDTO = new GradeExecutionDTO();
        gradeExecutionDTO.setId(UUID.randomUUID());
        gradeExecutionDTO.setStatus(GradeExecution.ExecutionStatus.PASSED);

        List<GradeExecutionDTO> gradeExecutionDTOList = Collections.singletonList(gradeExecutionDTO);
        Page<GradeExecutionDTO> gradeExecutionDTOPage = new PageImpl<>(gradeExecutionDTOList);

        when(gradeExecutionService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(gradeExecutionDTOPage); // Note: GradeExecutionService.findAll now returns
        // Page<GradeExecutionDTO>

        mockMvc.perform(get("/api/grade-executions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"GRADE_EXECUTION:CREATE"})
    public void createGradeExecution_ReturnsCreated() throws Exception {
        GradeExecutionDTO gradeExecutionDTO = new GradeExecutionDTO();
        gradeExecutionDTO.setPointsAwarded(BigDecimal.valueOf(10.0));
        gradeExecutionDTO.setStatus(GradeExecution.ExecutionStatus.PASSED);
        gradeExecutionDTO.setRubricGrade(UUID.randomUUID());
        gradeExecutionDTO.setSubmission(UUID.randomUUID());

        GradeExecutionDTO createdGradeExecutionDTO = new GradeExecutionDTO();
        createdGradeExecutionDTO.setId(UUID.randomUUID());
        createdGradeExecutionDTO.setPointsAwarded(BigDecimal.valueOf(10.0));
        createdGradeExecutionDTO.setStatus(GradeExecution.ExecutionStatus.PASSED);
        createdGradeExecutionDTO.setRubricGrade(UUID.randomUUID());
        createdGradeExecutionDTO.setSubmission(UUID.randomUUID());

        when(gradeExecutionService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdGradeExecutionDTO);

        mockMvc.perform(post("/api/grade-executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeExecutionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.pointsAwarded").value(10.0))
                .andExpect(jsonPath("$.data.status").value("PASSED"))
                .andExpect(jsonPath("$.data.rubricGrade").exists())
                .andExpect(jsonPath("$.data.submission").exists());
    }

    @Test
    @WithMockUser(authorities = {"GRADE_EXECUTION:READ"})
    public void getGradeExecution_ReturnsOk() throws Exception {
        GradeExecutionDTO gradeExecutionDTO = new GradeExecutionDTO();
        gradeExecutionDTO.setId(UUID.randomUUID());
        gradeExecutionDTO.setStatus(GradeExecution.ExecutionStatus.FAILED);

        when(gradeExecutionService.get(gradeExecutionDTO.getId())).thenReturn(gradeExecutionDTO);

        mockMvc.perform(get("/api/grade-executions/" + gradeExecutionDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(gradeExecutionDTO.getStatus().toString()));
    }

    @Test
    @WithMockUser(authorities = {"GRADE_EXECUTION:UPDATE"})
    public void updateGradeExecution_ReturnsOk() throws Exception {
        UUID gradeExecutionId = UUID.randomUUID();
        GradeExecutionDTO gradeExecutionDTO = new GradeExecutionDTO();
        gradeExecutionDTO.setStatus(GradeExecution.ExecutionStatus.ERROR);
        gradeExecutionDTO.setError("Some error");

        GradeExecutionDTO updatedGradeExecutionDTO = new GradeExecutionDTO();
        updatedGradeExecutionDTO.setId(gradeExecutionId);
        updatedGradeExecutionDTO.setPointsAwarded(BigDecimal.valueOf(5.0));
        updatedGradeExecutionDTO.setStatus(GradeExecution.ExecutionStatus.ERROR);
        updatedGradeExecutionDTO.setError("Some error");
        updatedGradeExecutionDTO.setRubricGrade(UUID.randomUUID());
        updatedGradeExecutionDTO.setSubmission(UUID.randomUUID());

        when(gradeExecutionService.update(eq(gradeExecutionId), any(GradeExecutionDTO.class)))
                .thenReturn(updatedGradeExecutionDTO);

        mockMvc.perform(patch("/api/grade-executions/" + gradeExecutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeExecutionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(gradeExecutionId.toString()))
                .andExpect(jsonPath("$.data.status").value("ERROR"))
                .andExpect(jsonPath("$.data.error").value("Some error"));
    }

    @Test
    @WithMockUser(authorities = {"GRADE_EXECUTION:DELETE"})
    public void deleteGradeExecution_ReturnsOk() throws Exception {
        UUID gradeExecutionId = UUID.randomUUID();
        doNothing().when(gradeExecutionService).delete(gradeExecutionId);

        mockMvc.perform(delete("/api/grade-executions/" + gradeExecutionId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"GRADE_EXECUTION:READ"})
    public void getGradeExecution_NotFound_ReturnsNotFound() throws Exception {
        UUID gradeExecutionId = UUID.randomUUID();
        when(gradeExecutionService.get(gradeExecutionId))
                .thenThrow(new EntityNotFoundException("GradeExecution not found"));

        mockMvc.perform(get("/api/grade-executions/" + gradeExecutionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"GRADE_EXECUTION:UPDATE"})
    public void updateGradeExecution_NotFound_ReturnsNotFound() throws Exception {
        UUID gradeExecutionId = UUID.randomUUID();
        GradeExecutionDTO gradeExecutionDTO = new GradeExecutionDTO();
        gradeExecutionDTO.setStatus(GradeExecution.ExecutionStatus.TIMEOUT);

        doThrow(new EntityNotFoundException("GradeExecution not found"))
                .when(gradeExecutionService)
                .update(org.mockito.ArgumentMatchers.eq(gradeExecutionId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/grade-executions/" + gradeExecutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeExecutionDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"GRADE_EXECUTION:DELETE"})
    public void deleteGradeExecution_NotFound_ReturnsNotFound() throws Exception {
        UUID gradeExecutionId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("GradeExecution not found")).when(gradeExecutionService)
                .delete(gradeExecutionId);

        mockMvc.perform(delete("/api/grade-executions/" + gradeExecutionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllGradeExecutions_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/grade-executions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getGradeExecutionById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID gradeExecutionId = UUID.randomUUID();
        mockMvc.perform(get("/api/grade-executions/" + gradeExecutionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createGradeExecution_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        GradeExecutionDTO gradeExecutionDTO = new GradeExecutionDTO();
        gradeExecutionDTO.setStatus(GradeExecution.ExecutionStatus.PENDING);
        gradeExecutionDTO.setRubricGrade(UUID.randomUUID());
        gradeExecutionDTO.setSubmission(UUID.randomUUID());

        mockMvc.perform(post("/api/grade-executions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeExecutionDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateGradeExecution_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID gradeExecutionId = UUID.randomUUID();
        GradeExecutionDTO gradeExecutionDTO = new GradeExecutionDTO();
        gradeExecutionDTO.setStatus(GradeExecution.ExecutionStatus.RUNNING);

        doThrow(new EntityNotFoundException("GradeExecution not found"))
                .when(gradeExecutionService)
                .update(org.mockito.ArgumentMatchers.eq(gradeExecutionId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/grade-executions/" + gradeExecutionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradeExecutionDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteGradeExecution_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID gradeExecutionId = UUID.randomUUID();

        mockMvc.perform(delete("/api/grade-executions/" + gradeExecutionId))
                .andExpect(status().isUnauthorized());
    }

    // Validation tests for GradeExecutionDTO would be needed here, similar to
    // UserResourceTest
    // @Test
    // @WithMockUser(authorities = {"GRADE_EXECUTION:CREATE"})
    // public void createGradeExecution_WithValidationError_ReturnsBadRequest()
    // throws Exception {
    // GradeExecutionDTO gradeExecutionDTO = new GradeExecutionDTO();
    // gradeExecutionDTO.setStatus(null); // Example validation error

    // mockMvc.perform(post("/api/grade-executions")
    // .contentType(MediaType.APPLICATION_JSON)
    // .content(objectMapper.writeValueAsString(gradeExecutionDTO)))
    // .andExpect(status().isBadRequest())
    // .andExpect(jsonPath("$.error.message").value("Validation failed"))
    // .andExpect(jsonPath("$.error.fieldErrors").isArray());
    // }

    // @Test
    // @WithMockUser(authorities = {"GRADE_EXECUTION:UPDATE"})
    // public void updateGradeExecution_WithValidationError_ReturnsBadRequest()
    // throws Exception {
    // UUID gradeExecutionId = UUID.randomUUID();
    // GradeExecutionDTO gradeExecutionDTO = new GradeExecutionDTO();
    // gradeExecutionDTO.setStatus(null); // Example validation error

    // mockMvc.perform(patch("/api/grade-executions/" + gradeExecutionId)
    // .contentType(MediaType.APPLICATION_JSON)
    // .content(objectMapper.writeValueAsString(gradeExecutionDTO)))
    // .andExpect(status().isBadRequest())
    // .andExpect(jsonPath("$.error.message").value("Validation failed"))
    // .andExpect(jsonPath("$.error.fieldErrors").isArray());
    // }
}
