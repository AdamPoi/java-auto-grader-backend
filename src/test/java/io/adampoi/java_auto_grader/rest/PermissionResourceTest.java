package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.PermissionDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.PermissionService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PermissionResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PermissionService permissionService;

    @Test
    @WithMockUser(authorities = {"PERMISSION:LIST"})
    public void getAllPermissions_ReturnsOk() throws Exception {
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setId(UUID.randomUUID());
        permissionDTO.setName("PERMISSION:TEST_READ");
        permissionDTO.setDescription("Test Permission");

        List<PermissionDTO> permissionDTOList = Collections.singletonList(permissionDTO);
        Page<PermissionDTO> permissionDTOPage = new PageImpl<>(permissionDTOList);

        when(permissionService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(PageResponse.from(permissionDTOPage));

        mockMvc.perform(get("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION:CREATE"})
    public void createPermission_ReturnsCreated() throws Exception {
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("PERMISSION:NEW_CREATE");
        permissionDTO.setDescription("Test Permission");

        PermissionDTO createdPermissionDTO = new PermissionDTO();
        createdPermissionDTO.setId(UUID.randomUUID());
        createdPermissionDTO.setName("PERMISSION:NEW_CREATE");
        createdPermissionDTO.setDescription("Test Permission");

        when(permissionService.create(org.mockito.ArgumentMatchers.any(PermissionDTO.class)))
                .thenReturn(createdPermissionDTO);

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("PERMISSION:NEW_CREATE"))
                .andExpect(jsonPath("$.data.description").value("Test Permission"));
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION:READ"})
    public void getPermission_ReturnsOk() throws Exception {
        UUID permissionId = UUID.randomUUID();
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setId(permissionId);
        permissionDTO.setName("PERMISSION:TEST_GET");
        permissionDTO.setDescription("Test Permission");

        when(permissionService.get(permissionId)).thenReturn(permissionDTO);

        mockMvc.perform(get("/api/permissions/" + permissionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(permissionDTO.getName()))
                .andExpect(jsonPath("$.data.description").value(permissionDTO.getDescription()));
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION:UPDATE"})
    public void updatePermission_ReturnsOk() throws Exception {
        UUID permissionId = UUID.randomUUID();
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setId(permissionId);
        permissionDTO.setName("PERMISSION:UPDATED");
        permissionDTO.setDescription("Test Permission");

        PermissionDTO updatedPermissionDTO = new PermissionDTO();
        updatedPermissionDTO.setId(permissionId);
        updatedPermissionDTO.setName("PERMISSION:UPDATED");
        updatedPermissionDTO.setDescription("Test Permission");

        when(permissionService.update(org.mockito.ArgumentMatchers.eq(permissionId),
                org.mockito.ArgumentMatchers.any(PermissionDTO.class)))
                .thenReturn(updatedPermissionDTO);

        mockMvc.perform(patch("/api/permissions/" + permissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(permissionId.toString()))
                .andExpect(jsonPath("$.data.name").value("PERMISSION:UPDATED"))
                .andExpect(jsonPath("$.data.description").value("Test Permission"));
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION:DELETE"})
    public void deletePermission_ReturnsNoContent() throws Exception {
        UUID permissionId = UUID.randomUUID();
        doNothing().when(permissionService).delete(permissionId);

        mockMvc.perform(delete("/api/permissions/" + permissionId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION:READ"})
    public void getPermission_NotFound_ReturnsNotFound() throws Exception {
        UUID permissionId = UUID.randomUUID();
        when(permissionService.get(permissionId))
                .thenThrow(new EntityNotFoundException("Permission not found"));

        mockMvc.perform(get("/api/permissions/" + permissionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION:UPDATE"})
    public void updatePermission_NotFound_ReturnsNotFound() throws Exception {
        UUID permissionId = UUID.randomUUID();
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("PERMISSION:NON_EXISTENT");
        permissionDTO.setDescription("Test Permission");
        permissionDTO.setDescription("Test Permission");

        doThrow(new EntityNotFoundException("Permission not found"))
                .when(permissionService).update(org.mockito.ArgumentMatchers.eq(permissionId),
                        org.mockito.ArgumentMatchers.any(PermissionDTO.class));

        mockMvc.perform(patch("/api/permissions/" + permissionId) // Using patch
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION:DELETE"})
    public void deletePermission_NotFound_ReturnsNotFound() throws Exception {
        UUID permissionId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Permission not found")).when(permissionService)
                .delete(permissionId);

        mockMvc.perform(delete("/api/permissions/" + permissionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {}) // No authority
    public void getAllPermissions_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {}) // No authority
    public void getPermissionById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID permissionId = UUID.randomUUID();
        mockMvc.perform(get("/api/permissions/" + permissionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {}) // No authority
    public void createPermission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("PERMISSION:Unauthorized_CREATE");
        permissionDTO.setDescription("Test Permission");

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {}) // No authority
    public void updatePermission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID permissionId = UUID.randomUUID();
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("PERMISSION:Unauthorized_UPDATE");
        permissionDTO.setDescription("Test Permission");

        mockMvc.perform(patch("/api/permissions/" + permissionId) // Using patch
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {}) // No authority
    public void deletePermission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID permissionId = UUID.randomUUID();

        mockMvc.perform(delete("/api/permissions/" + permissionId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION:CREATE"})
    public void createPermission_WithValidationError_ReturnsBadRequest() throws Exception {
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("");
        permissionDTO.setDescription("Test Permission");

        mockMvc.perform(post("/api/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION:UPDATE"})
    public void updatePermission_WithValidationError_ReturnsBadRequest() throws Exception {
        UUID permissionId = UUID.randomUUID();
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setName("");
        permissionDTO.setDescription("Test Permission");

        mockMvc.perform(patch("/api/permissions/" + permissionId) // Using patch
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(permissionDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }
}
