package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.RoleDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.service.RoleService;
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

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RoleResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleService roleService;

    @MockitoBean
    private PermissionRepository permissionRepository;

    @Test
    @WithMockUser(authorities = {"ROLE:LIST"})
    public void getAllRoles_ReturnsOk() throws Exception {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(UUID.randomUUID());
        roleDTO.setName("ADMIN");

        List<RoleDTO> roleDTOList = Collections.singletonList(roleDTO);
        Page<RoleDTO> roleDTOPage = new PageImpl<>(roleDTOList);

        when(roleService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(PageResponse.from(roleDTOPage));

        mockMvc.perform(get("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ROLE:CREATE"})
    public void createRole_ReturnsCreated() throws Exception {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("NEW_ROLE");

        RoleDTO createdRoleDTO = new RoleDTO();
        createdRoleDTO.setId(UUID.randomUUID());
        createdRoleDTO.setName("NEW_ROLE");

        when(roleService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdRoleDTO);

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value(createdRoleDTO.getName()));
    }

    @Test
    @WithMockUser(authorities = {"ROLE:READ"})
    public void getRole_ReturnsOk() throws Exception {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(UUID.randomUUID());
        roleDTO.setName("USER");

        when(roleService.get(roleDTO.getId())).thenReturn(roleDTO);

        mockMvc.perform(get("/api/roles/" + roleDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(roleDTO.getName()));
    }

    @Test
    @WithMockUser(authorities = {"ROLE:UPDATE"})
    public void updateRole_ReturnsOk() throws Exception {
        UUID roleId = UUID.randomUUID();
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(roleId);
        roleDTO.setName("UPDATED_ROLE");

        RoleDTO updatedRoleDTO = new RoleDTO();
        updatedRoleDTO.setId(roleId);
        updatedRoleDTO.setName("UPDATED_ROLE");

        when(roleService.update(org.mockito.ArgumentMatchers.eq(roleId), org.mockito.ArgumentMatchers.any()))
                .thenReturn(updatedRoleDTO);

        mockMvc.perform(patch("/api/roles/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(updatedRoleDTO.getName()));
    }

    @Test
    @WithMockUser(authorities = {"ROLE:DELETE"})
    public void deleteRole_ReturnsOk() throws Exception {
        UUID roleId = UUID.randomUUID();
        doNothing().when(roleService).delete(roleId);

        mockMvc.perform(delete("/api/roles/" + roleId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"ROLE:READ"})
    public void getRole_NotFound_ReturnsNotFound() throws Exception {
        UUID roleId = UUID.randomUUID();
        when(roleService.get(roleId)).thenThrow(new EntityNotFoundException("Role not found"));

        mockMvc.perform(get("/api/roles/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"ROLE:UPDATE"})
    public void updateRole_NotFound_ReturnsNotFound() throws Exception {
        UUID roleId = UUID.randomUUID();
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("UPDATED_ROLE");

        doThrow(new EntityNotFoundException("Role not found"))
                .when(roleService)
                .update(org.mockito.ArgumentMatchers.eq(roleId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/roles/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"ROLE:DELETE"})
    public void deleteRole_NotFound_ReturnsNotFound() throws Exception {
        UUID roleId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Role not found")).when(roleService).delete(roleId);

        mockMvc.perform(delete("/api/roles/" + roleId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllRoles_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getRoleById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID roleId = UUID.randomUUID();
        mockMvc.perform(get("/api/roles/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createRole_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("NEW_ROLE");

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateRole_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID roleId = UUID.randomUUID();
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("UPDATED_ROLE");

        mockMvc.perform(patch("/api/roles/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteRole_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID roleId = UUID.randomUUID();

        mockMvc.perform(delete("/api/roles/" + roleId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"ROLE:CREATE"})
    public void createRole_WithValidationError_ReturnsBadRequest() throws Exception {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("");

        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"ROLE:UPDATE"})
    public void updateRole_WithValidationError_ReturnsBadRequest() throws Exception {
        UUID roleId = UUID.randomUUID();
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName("");

        mockMvc.perform(patch("/api/roles/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"ROLE:READ"})
    public void getRolePermissionsValues_ReturnsOk() throws Exception {
        Map<UUID, String> permissions = new HashMap<>();
        permissions.put(UUID.randomUUID(), "PERMISSION_READ");
        permissions.put(UUID.randomUUID(), "PERMISSION_WRITE");

        when(permissionRepository
                .findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Sort.class)))
                .thenReturn(Collections.emptyList()); // Mocking to avoid null pointer

        mockMvc.perform(get("/api/roles/permissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
