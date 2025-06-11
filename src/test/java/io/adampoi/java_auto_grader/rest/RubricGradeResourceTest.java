package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.domain.RubricGrade;
import io.adampoi.java_auto_grader.model.dto.RubricGradeDTO;
import io.adampoi.java_auto_grader.service.RubricGradeService;
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
public class RubricGradeResourceTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private RubricGradeService rubricGradeService;

    @Test
    @WithMockUser(authorities = {"RUBRIC_GRADE:LIST"})
    public void getAllRubricGrades_ReturnsOk() throws Exception {
        RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
        rubricGradeDTO.setId(UUID.randomUUID());
        rubricGradeDTO.setName("Test Rubric Grade");

        List<RubricGradeDTO> rubricGradeDTOList = Collections.singletonList(rubricGradeDTO);
        Page<RubricGradeDTO> rubricGradeDTOPage = new PageImpl<>(rubricGradeDTOList);

        when(rubricGradeService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(rubricGradeDTOPage);

        mockMvc.perform(get("/api/rubric-grades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC_GRADE:CREATE"})
    public void createRubricGrade_ReturnsCreated() throws Exception {
        RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
        rubricGradeDTO.setName("New Rubric Grade");
        rubricGradeDTO.setDescription("Description");
        rubricGradeDTO.setPoints(BigDecimal.valueOf(10.0));
        rubricGradeDTO.setDisplayOrder(1);
        rubricGradeDTO.setCode("CODE123");
        rubricGradeDTO.setGradeType(RubricGrade.GradeType.FUNCTIONALITY);
        rubricGradeDTO.setRubric(UUID.randomUUID());

        RubricGradeDTO createdRubricGradeDTO = new RubricGradeDTO();
        createdRubricGradeDTO.setId(UUID.randomUUID());
        createdRubricGradeDTO.setName("New Rubric Grade");
        createdRubricGradeDTO.setDescription("Description");
        createdRubricGradeDTO.setPoints(BigDecimal.valueOf(10.0));
        createdRubricGradeDTO.setDisplayOrder(1);
        createdRubricGradeDTO.setCode("CODE123");
        createdRubricGradeDTO.setGradeType(RubricGrade.GradeType.FUNCTIONALITY);
        createdRubricGradeDTO.setRubric(UUID.randomUUID());

        when(rubricGradeService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdRubricGradeDTO);

        mockMvc.perform(post("/api/rubric-grades")
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
    @WithMockUser(authorities = {"RUBRIC_GRADE:READ"})
    public void getRubricGrade_ReturnsOk() throws Exception {
        RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
        rubricGradeDTO.setId(UUID.randomUUID());
        rubricGradeDTO.setName("Test Rubric Grade");

        when(rubricGradeService.get(rubricGradeDTO.getId())).thenReturn(rubricGradeDTO);

        mockMvc.perform(get("/api/rubric-grades/" + rubricGradeDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(rubricGradeDTO.getName()));
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC_GRADE:UPDATE"})
    public void updateRubricGrade_ReturnsOk() throws Exception {
        UUID rubricGradeId = UUID.randomUUID();
        RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
        rubricGradeDTO.setName("Updated Rubric Grade");
        rubricGradeDTO.setDescription("Updated Description");

        RubricGradeDTO updatedRubricGradeDTO = new RubricGradeDTO();
        updatedRubricGradeDTO.setId(rubricGradeId);
        updatedRubricGradeDTO.setName("Updated Rubric Grade");
        updatedRubricGradeDTO.setDescription("Updated Description");
        updatedRubricGradeDTO.setPoints(BigDecimal.valueOf(15.0));
        updatedRubricGradeDTO.setDisplayOrder(2);
        updatedRubricGradeDTO.setCode("UPDATED_CODE");
        updatedRubricGradeDTO.setGradeType(RubricGrade.GradeType.COMPILATION);
        updatedRubricGradeDTO.setRubric(UUID.randomUUID());

        when(rubricGradeService.update(eq(rubricGradeId), any(RubricGradeDTO.class))).thenReturn(updatedRubricGradeDTO);

        mockMvc.perform(patch("/api/rubric-grades/" + rubricGradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(rubricGradeId.toString()))
                .andExpect(jsonPath("$.data.name").value("Updated Rubric Grade"))
                .andExpect(jsonPath("$.data.description").value("Updated Description"));
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC_GRADE:DELETE"})
    public void deleteRubricGrade_ReturnsOk() throws Exception {
        UUID rubricGradeId = UUID.randomUUID();
        doNothing().when(rubricGradeService).delete(rubricGradeId);

        mockMvc.perform(delete("/api/rubric-grades/" + rubricGradeId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC_GRADE:READ"})
    public void getRubricGrade_NotFound_ReturnsNotFound() throws Exception {
        UUID rubricGradeId = UUID.randomUUID();
        when(rubricGradeService.get(rubricGradeId)).thenThrow(new EntityNotFoundException("RubricGrade not found"));

        mockMvc.perform(get("/api/rubric-grades/" + rubricGradeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC_GRADE:UPDATE"})
    public void updateRubricGrade_NotFound_ReturnsNotFound() throws Exception {
        UUID rubricGradeId = UUID.randomUUID();
        RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
        rubricGradeDTO.setName("Updated Rubric Grade");

        doThrow(new EntityNotFoundException("RubricGrade not found"))
                .when(rubricGradeService)
                .update(org.mockito.ArgumentMatchers.eq(rubricGradeId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/rubric-grades/" + rubricGradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC_GRADE:DELETE"})
    public void deleteRubricGrade_NotFound_ReturnsNotFound() throws Exception {
        UUID rubricGradeId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("RubricGrade not found")).when(rubricGradeService).delete(rubricGradeId);

        mockMvc.perform(delete("/api/rubric-grades/" + rubricGradeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllRubricGrades_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/rubric-grades")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getRubricGradeById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID rubricGradeId = UUID.randomUUID();
        mockMvc.perform(get("/api/rubric-grades/" + rubricGradeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createRubricGrade_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
        rubricGradeDTO.setName("New Rubric Grade");

        mockMvc.perform(post("/api/rubric-grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateRubricGrade_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID rubricGradeId = UUID.randomUUID();
        RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
        rubricGradeDTO.setName("Updated Rubric Grade");

        doThrow(new EntityNotFoundException("RubricGrade not found"))
                .when(rubricGradeService)
                .update(org.mockito.ArgumentMatchers.eq(rubricGradeId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/rubric-grades/" + rubricGradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteRubricGrade_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID rubricGradeId = UUID.randomUUID();

        mockMvc.perform(delete("/api/rubric-grades/" + rubricGradeId))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(authorities = {"RUBRIC_GRADE:CREATE"})
    public void createRubricGrade_WithValidationError_ReturnsBadRequest() throws
            Exception {
        RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
        rubricGradeDTO.setName("");

        mockMvc.perform(post("/api/rubric-grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC_GRADE:UPDATE"})
    public void updateRubricGrade_WithValidationError_ReturnsBadRequest() throws
            Exception {
        UUID rubricGradeId = UUID.randomUUID();
        RubricGradeDTO rubricGradeDTO = new RubricGradeDTO();
        rubricGradeDTO.setName("");

        mockMvc.perform(patch("/api/rubric-grades/" + rubricGradeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricGradeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }
}
