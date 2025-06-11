package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.AssignmentDTO;
import io.adampoi.java_auto_grader.service.AssignmentService;
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

import java.time.OffsetDateTime;
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
public class AssignmentResourceTest {

    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AssignmentService assignmentService;

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:LIST"})
    public void getAllAssignments_ReturnsOk() throws Exception {
        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setId(UUID.randomUUID());
        assignmentDTO.setTitle("Test Assignment");

        List<AssignmentDTO> assignmentDTOList = Collections.singletonList(assignmentDTO);
        Page<AssignmentDTO> assignmentDTOPage = new PageImpl<>(assignmentDTOList);

        when(assignmentService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(assignmentDTOPage);

        mockMvc.perform(get("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:CREATE"})
    public void createAssignment_ReturnsCreated() throws Exception {
        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setTitle("New Assignment");
        assignmentDTO.setDescription("Description");
        assignmentDTO.setDueDate(OffsetDateTime.now().plusDays(7));
        assignmentDTO.setIsPublished(true);
        assignmentDTO.setMaxAttempts(3);
        assignmentDTO.setCourse(UUID.randomUUID());
        assignmentDTO.setCreatedByTeacher(UUID.randomUUID());

        AssignmentDTO createdAssignmentDTO = new AssignmentDTO();
        createdAssignmentDTO.setId(UUID.randomUUID());
        createdAssignmentDTO.setTitle("New Assignment");
        createdAssignmentDTO.setDescription("Description");
        createdAssignmentDTO.setDueDate(OffsetDateTime.now().plusDays(7));
        createdAssignmentDTO.setIsPublished(true);
        createdAssignmentDTO.setMaxAttempts(3);
        createdAssignmentDTO.setCourse(UUID.randomUUID());
        createdAssignmentDTO.setCreatedByTeacher(UUID.randomUUID());

        when(assignmentService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdAssignmentDTO);

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value("New Assignment"))
                .andExpect(jsonPath("$.data.description").value("Description"))
                .andExpect(jsonPath("$.data.isPublished").value(true))
                .andExpect(jsonPath("$.data.maxAttempts").value(3))
                .andExpect(jsonPath("$.data.course").exists())
                .andExpect(jsonPath("$.data.createdByTeacher").exists());
    }

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:READ"})
    public void getAssignment_ReturnsOk() throws Exception {
        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setId(UUID.randomUUID());
        assignmentDTO.setTitle("Test Assignment");

        when(assignmentService.get(assignmentDTO.getId())).thenReturn(assignmentDTO);

        mockMvc.perform(get("/api/assignments/" + assignmentDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(assignmentDTO.getTitle()));
    }

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:UPDATE"})
    public void updateAssignment_ReturnsOk() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setTitle("Updated Assignment");
        assignmentDTO.setDescription("Updated Description");

        AssignmentDTO updatedAssignmentDTO = new AssignmentDTO();
        updatedAssignmentDTO.setId(assignmentId);
        updatedAssignmentDTO.setTitle("Updated Assignment");
        updatedAssignmentDTO.setDescription("Updated Description");
        updatedAssignmentDTO.setDueDate(OffsetDateTime.now().plusDays(14));
        updatedAssignmentDTO.setIsPublished(false);
        updatedAssignmentDTO.setMaxAttempts(5);
        updatedAssignmentDTO.setCourse(UUID.randomUUID());
        updatedAssignmentDTO.setCreatedByTeacher(UUID.randomUUID());

        when(assignmentService.update(eq(assignmentId), any(AssignmentDTO.class))).thenReturn(updatedAssignmentDTO);

        mockMvc.perform(patch("/api/assignments/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(assignmentId.toString()))
                .andExpect(jsonPath("$.data.title").value("Updated Assignment"))
                .andExpect(jsonPath("$.data.description").value("Updated Description"));
    }

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:DELETE"})
    public void deleteAssignment_ReturnsOk() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        doNothing().when(assignmentService).delete(assignmentId);

        mockMvc.perform(delete("/api/assignments/" + assignmentId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:READ"})
    public void getAssignment_NotFound_ReturnsNotFound() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        when(assignmentService.get(assignmentId)).thenThrow(new EntityNotFoundException("Assignment not found"));

        mockMvc.perform(get("/api/assignments/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:UPDATE"})
    public void updateAssignment_NotFound_ReturnsNotFound() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setTitle("Updated Assignment");

        doThrow(new EntityNotFoundException("Assignment not found"))
                .when(assignmentService)
                .update(org.mockito.ArgumentMatchers.eq(assignmentId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/assignments/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:DELETE"})
    public void deleteAssignment_NotFound_ReturnsNotFound() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Assignment not found")).when(assignmentService).delete(assignmentId);

        mockMvc.perform(delete("/api/assignments/" + assignmentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllAssignments_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAssignmentById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        mockMvc.perform(get("/api/assignments/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createAssignment_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setTitle("New Assignment");
        assignmentDTO.setDueDate(OffsetDateTime.now());
        assignmentDTO.setCourse(UUID.randomUUID());
        assignmentDTO.setCreatedByTeacher(UUID.randomUUID());

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateAssignment_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID assignmentId = UUID.randomUUID();
        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setTitle("Updated Assignment");

        doThrow(new EntityNotFoundException("Assignment not found"))
                .when(assignmentService)
                .update(org.mockito.ArgumentMatchers.eq(assignmentId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/assignments/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteAssignment_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID assignmentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/assignments/" + assignmentId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:CREATE"})
    public void createAssignment_WithValidationError_ReturnsBadRequest() throws
            Exception {
        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setTitle(""); // Example validation error

        mockMvc.perform(post("/api/assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"ASSIGNMENT:UPDATE"})
    public void updateAssignment_WithValidationError_ReturnsBadRequest() throws
            Exception {
        UUID assignmentId = UUID.randomUUID();
        AssignmentDTO assignmentDTO = new AssignmentDTO();
        assignmentDTO.setTitle(""); // Example validation error

        mockMvc.perform(patch("/api/assignments/" + assignmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }
}
