package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.SubmissionCodeDTO;
import io.adampoi.java_auto_grader.service.SubmissionCodeService;
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
public class SubmissionCodeResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private SubmissionCodeService submissionCodeService;

    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:LIST"})
    public void getAllSubmissionCodes_ReturnsOk() throws Exception {
        SubmissionCodeDTO submissionCodeDTO = new SubmissionCodeDTO();
        submissionCodeDTO.setId(UUID.randomUUID());
        submissionCodeDTO.setFileName("Test.java");

        List<SubmissionCodeDTO> submissionCodeDTOList = Collections.singletonList(submissionCodeDTO);
        Page<SubmissionCodeDTO> submissionCodeDTOPage = new PageImpl<>(submissionCodeDTOList);

        when(submissionCodeService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(submissionCodeDTOPage);

        mockMvc.perform(get("/api/submission-codes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:CREATE"})
    public void createSubmissionCode_ReturnsCreated() throws Exception {
        SubmissionCodeDTO submissionCodeDTO = new SubmissionCodeDTO();
        submissionCodeDTO.setFileName("NewFile.java");
        submissionCodeDTO.setSourceCode("public class NewFile {}");
        submissionCodeDTO.setPackagePath("com.example");
        submissionCodeDTO.setClassName("NewFile");
        submissionCodeDTO.setSubmission(UUID.randomUUID());

        SubmissionCodeDTO createdSubmissionCodeDTO = new SubmissionCodeDTO();
        createdSubmissionCodeDTO.setId(UUID.randomUUID());
        createdSubmissionCodeDTO.setFileName("NewFile.java");
        createdSubmissionCodeDTO.setSourceCode("public class NewFile {}");
        createdSubmissionCodeDTO.setPackagePath("com.example");
        createdSubmissionCodeDTO.setClassName("NewFile");
        createdSubmissionCodeDTO.setSubmission(UUID.randomUUID());

        when(submissionCodeService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdSubmissionCodeDTO);

        mockMvc.perform(post("/api/submission-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionCodeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.fileName").value("NewFile.java"))
                .andExpect(jsonPath("$.data.sourceCode").value("public class NewFile {}"))
                .andExpect(jsonPath("$.data.packagePath").value("com.example"))
                .andExpect(jsonPath("$.data.className").value("NewFile"))
                .andExpect(jsonPath("$.data.submission").exists());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:READ"})
    public void getSubmissionCode_ReturnsOk() throws Exception {
        SubmissionCodeDTO submissionCodeDTO = new SubmissionCodeDTO();
        submissionCodeDTO.setId(UUID.randomUUID());
        submissionCodeDTO.setFileName("ExistingFile.java");

        when(submissionCodeService.get(submissionCodeDTO.getId())).thenReturn(submissionCodeDTO);

        mockMvc.perform(get("/api/submission-codes/" + submissionCodeDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value(submissionCodeDTO.getFileName()));
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:UPDATE"})
    public void updateSubmissionCode_ReturnsOk() throws Exception {
        UUID submissionCodeId = UUID.randomUUID();
        SubmissionCodeDTO submissionCodeDTO = new SubmissionCodeDTO();
        submissionCodeDTO.setFileName("UpdatedFile.java");
        submissionCodeDTO.setSourceCode("public class UpdatedFile {}");

        SubmissionCodeDTO updatedSubmissionCodeDTO = new SubmissionCodeDTO();
        updatedSubmissionCodeDTO.setId(submissionCodeId);
        updatedSubmissionCodeDTO.setFileName("UpdatedFile.java");
        updatedSubmissionCodeDTO.setSourceCode("public class UpdatedFile {}");
        updatedSubmissionCodeDTO.setPackagePath("com.updated");
        updatedSubmissionCodeDTO.setClassName("UpdatedFile");
        updatedSubmissionCodeDTO.setSubmission(UUID.randomUUID());

        when(submissionCodeService.update(eq(submissionCodeId), any(SubmissionCodeDTO.class)))
                .thenReturn(updatedSubmissionCodeDTO);

        mockMvc.perform(patch("/api/submission-codes/" + submissionCodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionCodeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(submissionCodeId.toString()))
                .andExpect(jsonPath("$.data.fileName").value("UpdatedFile.java"))
                .andExpect(jsonPath("$.data.sourceCode").value("public class UpdatedFile {}"));
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:DELETE"})
    public void deleteSubmissionCode_ReturnsOk() throws Exception {
        UUID submissionCodeId = UUID.randomUUID();
        doNothing().when(submissionCodeService).delete(submissionCodeId);

        mockMvc.perform(delete("/api/submission-codes/" + submissionCodeId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:READ"})
    public void getSubmissionCode_NotFound_ReturnsNotFound() throws Exception {
        UUID submissionCodeId = UUID.randomUUID();
        when(submissionCodeService.get(submissionCodeId))
                .thenThrow(new EntityNotFoundException("SubmissionCode not found"));

        mockMvc.perform(get("/api/submission-codes/" + submissionCodeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:UPDATE"})
    public void updateSubmissionCode_NotFound_ReturnsNotFound() throws Exception {
        UUID submissionCodeId = UUID.randomUUID();
        SubmissionCodeDTO submissionCodeDTO = new SubmissionCodeDTO();
        submissionCodeDTO.setFileName("NonExistent.java");

        doThrow(new EntityNotFoundException("SubmissionCode not found"))
                .when(submissionCodeService)
                .update(org.mockito.ArgumentMatchers.eq(submissionCodeId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/submission-codes/" + submissionCodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionCodeDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:DELETE"})
    public void deleteSubmissionCode_NotFound_ReturnsNotFound() throws Exception {
        UUID submissionCodeId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("SubmissionCode not found")).when(submissionCodeService)
                .delete(submissionCodeId);

        mockMvc.perform(delete("/api/submission-codes/" + submissionCodeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllSubmissionCodes_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/submission-codes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getSubmissionCodeById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID submissionCodeId = UUID.randomUUID();
        mockMvc.perform(get("/api/submission-codes/" + submissionCodeId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createSubmissionCode_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        SubmissionCodeDTO submissionCodeDTO = new SubmissionCodeDTO();
        submissionCodeDTO.setSubmission(UUID.randomUUID());
        submissionCodeDTO.setFileName("HelloWorld.java");
        submissionCodeDTO.setSourceCode("public class HelloWorld {  }");

        mockMvc.perform(post("/api/submission-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionCodeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateSubmissionCode_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID submissionCodeId = UUID.randomUUID();
        SubmissionCodeDTO submissionCodeDTO = new SubmissionCodeDTO();
        submissionCodeDTO.setFileName("UnauthorizedUpdate.java");

        doThrow(new EntityNotFoundException("SubmissionCode not found"))
                .when(submissionCodeService)
                .update(org.mockito.ArgumentMatchers.eq(submissionCodeId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/submission-codes/" + submissionCodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionCodeDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteSubmissionCode_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID submissionCodeId = UUID.randomUUID();

        mockMvc.perform(delete("/api/submission-codes/" + submissionCodeId))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:CREATE"})
    public void createSubmissionCode_WithValidationError_ReturnsBadRequest()
            throws Exception {
        SubmissionCodeDTO submissionCodeDTO = new SubmissionCodeDTO();
        submissionCodeDTO.setFileName("");

        mockMvc.perform(post("/api/submission-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionCodeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"SUBMISSION_CODE:UPDATE"})
    public void updateSubmissionCode_WithValidationError_ReturnsBadRequest()
            throws Exception {
        UUID submissionCodeId = UUID.randomUUID();
        SubmissionCodeDTO submissionCodeDTO = new SubmissionCodeDTO();
        submissionCodeDTO.setFileName("");

        mockMvc.perform(patch("/api/submission-codes/" + submissionCodeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submissionCodeDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }
}
