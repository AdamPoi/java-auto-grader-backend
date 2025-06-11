package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.SubmissionDTO;
import io.adampoi.java_auto_grader.service.SubmissionService;
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
public class SubmissionResourceTest {


    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private SubmissionService submissionService;

    @Test
    @WithMockUser(authorities = {"SUBMISSION:LIST"})
    public void getAllSubmissions_ReturnsOk() throws Exception {
        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setId(UUID.randomUUID());
        submissionDTO.setStatus("GRADED");

        List<SubmissionDTO> submissionDTOList = Collections.singletonList(submissionDTO);
        Page<SubmissionDTO> submissionDTOPage = new PageImpl<>(submissionDTOList);

        when(submissionService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(submissionDTOPage);

        mockMvc.perform(get("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION:CREATE"})
    public void createSubmission_ReturnsCreated() throws Exception {
        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setSubmissionTime(OffsetDateTime.now());
        submissionDTO.setAttemptNumber(1);
        submissionDTO.setStatus("PENDING");
        submissionDTO.setAssignment(UUID.randomUUID());
        submissionDTO.setStudent(UUID.randomUUID());


        SubmissionDTO createdSubmissionDTO = new SubmissionDTO();
        createdSubmissionDTO.setId(UUID.randomUUID());
        createdSubmissionDTO.setSubmissionTime(OffsetDateTime.now());
        createdSubmissionDTO.setAttemptNumber(1);
        createdSubmissionDTO.setStatus("PENDING");
        createdSubmissionDTO.setAssignment(UUID.randomUUID());
        createdSubmissionDTO.setStudent(UUID.randomUUID());

        when(submissionService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdSubmissionDTO);

        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.attemptNumber").value(1))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.assignment").exists())
                .andExpect(jsonPath("$.data.student").exists());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION:READ"})
    public void getSubmission_ReturnsOk() throws Exception {
        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setId(UUID.randomUUID());
        submissionDTO.setStatus("GRADED");

        when(submissionService.get(submissionDTO.getId())).thenReturn(submissionDTO);

        mockMvc.perform(get("/api/submissions/" + submissionDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(submissionDTO.getStatus()));
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION:UPDATE"})
    public void updateSubmission_ReturnsOk() throws Exception {
        UUID submissionId = UUID.randomUUID();
        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setStatus("COMPLETED");
        submissionDTO.setGraderFeedback("Good job!");
        submissionDTO.setAttemptNumber(2);

        SubmissionDTO updatedSubmissionDTO = new SubmissionDTO();
        updatedSubmissionDTO.setId(submissionId);
        updatedSubmissionDTO.setStatus("COMPLETED");
        updatedSubmissionDTO.setGraderFeedback("Good job!");
        updatedSubmissionDTO.setAttemptNumber(2);
        updatedSubmissionDTO.setAssignment(UUID.randomUUID());
        updatedSubmissionDTO.setStudent(UUID.randomUUID());

        when(submissionService.update(eq(submissionId), any(SubmissionDTO.class))).thenReturn(updatedSubmissionDTO);

        mockMvc.perform(patch("/api/submissions/" + submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(submissionId.toString()))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.graderFeedback").value("Good job!"));
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION:DELETE"})
    public void deleteSubmission_ReturnsOk() throws Exception {
        UUID submissionId = UUID.randomUUID();
        doNothing().when(submissionService).delete(submissionId);

        mockMvc.perform(delete("/api/submissions/" + submissionId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION:READ"})
    public void getSubmission_NotFound_ReturnsNotFound() throws Exception {
        UUID submissionId = UUID.randomUUID();
        when(submissionService.get(submissionId)).thenThrow(new EntityNotFoundException("Submission not found"));

        mockMvc.perform(get("/api/submissions/" + submissionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION:UPDATE"})
    public void updateSubmission_NotFound_ReturnsNotFound() throws Exception {
        UUID submissionId = UUID.randomUUID();
        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setStatus("FAILED");
        submissionDTO.setAttemptNumber(1);

        doThrow(new EntityNotFoundException("Submission not found"))
                .when(submissionService)
                .update(org.mockito.ArgumentMatchers.eq(submissionId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/submissions/" + submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION:DELETE"})
    public void deleteSubmission_NotFound_ReturnsNotFound() throws Exception {
        UUID submissionId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Submission not found")).when(submissionService).delete(submissionId);

        mockMvc.perform(delete("/api/submissions/" + submissionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllSubmissions_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getSubmissionById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID submissionId = UUID.randomUUID();
        mockMvc.perform(get("/api/submissions/" + submissionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createSubmission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setSubmissionTime(OffsetDateTime.now());
        submissionDTO.setAttemptNumber(1);
        submissionDTO.setStatus("PENDING");
        submissionDTO.setAssignment(UUID.randomUUID());
        submissionDTO.setStudent(UUID.randomUUID());

        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateSubmission_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID submissionId = UUID.randomUUID();
        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setStatus("COMPLETED");
        submissionDTO.setAttemptNumber(2);

        doThrow(new EntityNotFoundException("Submission not found"))
                .when(submissionService)
                .update(org.mockito.ArgumentMatchers.eq(submissionId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/submissions/" + submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteSubmission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID submissionId = UUID.randomUUID();

        mockMvc.perform(delete("/api/submissions/" + submissionId))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(authorities = {"SUBMISSION:CREATE"})
    public void createSubmission_WithValidationError_ReturnsBadRequest() throws
            Exception {
        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setStatus(null);

        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION:UPDATE"})
    public void updateSubmission_WithValidationError_ReturnsBadRequest() throws
            Exception {
        UUID submissionId = UUID.randomUUID();
        SubmissionDTO submissionDTO = new SubmissionDTO();

        mockMvc.perform(patch("/api/submissions/" + submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }
}
