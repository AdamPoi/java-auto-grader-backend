package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.RubricDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.RubricService;
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
public class RubricResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private RubricService rubricService;

    @Test
    @WithMockUser(authorities = {"RUBRIC:LIST"})
    public void getAllRubrics_ReturnsOk() throws Exception {
        RubricDTO rubricDTO = new RubricDTO();
        rubricDTO.setId(UUID.randomUUID());
        rubricDTO.setName("Test Rubric");

        List<RubricDTO> rubricDTOList = Collections.singletonList(rubricDTO);
        Page<RubricDTO> rubricDTOPage = new PageImpl<>(rubricDTOList);

        when(rubricService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(PageResponse.from(rubricDTOPage));

        mockMvc.perform(get("/api/rubrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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

    @Test
    @WithMockUser(authorities = {"RUBRIC:UPDATE"})
    public void updateRubric_ReturnsOk() throws Exception {
        UUID rubricId = UUID.randomUUID();
        RubricDTO rubricDTO = new RubricDTO();
        rubricDTO.setName("Updated Rubric");
        rubricDTO.setDescription("Updated Description");

        RubricDTO updatedRubricDTO = new RubricDTO();
        updatedRubricDTO.setId(rubricId);
        updatedRubricDTO.setName("Updated Rubric");
        updatedRubricDTO.setDescription("Updated Description");
        updatedRubricDTO.setMaxPoints(BigDecimal.TEN);
        updatedRubricDTO.setDisplayOrder(1);
        updatedRubricDTO.setIsActive(true);
        updatedRubricDTO.setAssignment(UUID.randomUUID());

        when(rubricService.update(eq(rubricId), any(RubricDTO.class))).thenReturn(updatedRubricDTO);

        mockMvc.perform(patch("/api/rubrics/" + rubricId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(rubricId.toString()))
                .andExpect(jsonPath("$.data.name").value("Updated Rubric"))
                .andExpect(jsonPath("$.data.description").value("Updated Description"));
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC:DELETE"})
    public void deleteRubric_ReturnsOk() throws Exception {
        UUID rubricId = UUID.randomUUID();
        doNothing().when(rubricService).delete(rubricId);

        mockMvc.perform(delete("/api/rubrics/" + rubricId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC:READ"})
    public void getRubric_NotFound_ReturnsNotFound() throws Exception {
        UUID rubricId = UUID.randomUUID();
        when(rubricService.get(rubricId)).thenThrow(new EntityNotFoundException("Rubric not found"));

        mockMvc.perform(get("/api/rubrics/" + rubricId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC:UPDATE"})
    public void updateRubric_NotFound_ReturnsNotFound() throws Exception {
        UUID rubricId = UUID.randomUUID();
        RubricDTO rubricDTO = new RubricDTO();
        rubricDTO.setName("Updated Rubric");

        doThrow(new EntityNotFoundException("Rubric not found"))
                .when(rubricService)
                .update(org.mockito.ArgumentMatchers.eq(rubricId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/rubrics/" + rubricId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC:DELETE"})
    public void deleteRubric_NotFound_ReturnsNotFound() throws Exception {
        UUID rubricId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Rubric not found")).when(rubricService).delete(rubricId);

        mockMvc.perform(delete("/api/rubrics/" + rubricId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllRubrics_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/rubrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getRubricById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID rubricId = UUID.randomUUID();
        mockMvc.perform(get("/api/rubrics/" + rubricId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createRubric_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        RubricDTO rubricDTO = new RubricDTO();
        rubricDTO.setName("New Rubric");

        mockMvc.perform(post("/api/rubrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateRubric_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID rubricId = UUID.randomUUID();
        RubricDTO rubricDTO = new RubricDTO();
        rubricDTO.setName("Updated Rubric");

        doThrow(new EntityNotFoundException("Rubric not found"))
                .when(rubricService)
                .update(org.mockito.ArgumentMatchers.eq(rubricId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/rubrics/" + rubricId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteRubric_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID rubricId = UUID.randomUUID();

        mockMvc.perform(delete("/api/rubrics/" + rubricId))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(authorities = {"RUBRIC:CREATE"})
    public void createRubric_WithValidationError_ReturnsBadRequest() throws
            Exception {
        RubricDTO rubricDTO = new RubricDTO();
        rubricDTO.setName("");

        mockMvc.perform(post("/api/rubrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"RUBRIC:UPDATE"})
    public void updateRubric_WithValidationError_ReturnsBadRequest() throws
            Exception {
        UUID rubricId = UUID.randomUUID();
        RubricDTO rubricDTO = new RubricDTO();
        rubricDTO.setName("");

        mockMvc.perform(patch("/api/rubrics/" + rubricId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rubricDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }
}
