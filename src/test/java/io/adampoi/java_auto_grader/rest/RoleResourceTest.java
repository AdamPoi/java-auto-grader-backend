package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.RoleDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.service.RoleService;
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
class RoleResourceTest {

    private static final String BASE_API_PATH = "/api/roles";
    private static final String AUTHORITY_LIST = "ROLE:LIST";
    private static final String AUTHORITY_CREATE = "ROLE:CREATE";
    private static final String AUTHORITY_READ = "ROLE:READ";
    private static final String AUTHORITY_UPDATE = "ROLE:UPDATE";
    private static final String AUTHORITY_DELETE = "ROLE:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private RoleService roleService;
    @MockitoBean
    private PermissionRepository permissionRepository;

    private UUID testRoleId;
    private RoleDTO testRoleDTO;

    @BeforeEach
    void setUp() {
        testRoleId = UUID.randomUUID();
        testRoleDTO = createRoleDTO("TEST_ROLE");
    }

    private RoleDTO createRoleDTO(String name) {
        RoleDTO dto = new RoleDTO();
        dto.setName(name);
        return dto;
    }

    private RoleDTO createRoleDTOWithId(UUID id, String name) {
        RoleDTO dto = new RoleDTO();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }

    @Test
    @WithMockUser(authorities = {AUTHORITY_READ})
    @DisplayName("GET /api/roles/permissions - should return 200 OK with permissions")
    void getRolePermissionsValues_ReturnsOk() throws Exception {
        when(permissionRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get(BASE_API_PATH + "/permissions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Nested
    @DisplayName("GET /api/roles")
    class GetAllRoles {

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 OK with roles")
        void getAllRoles_ReturnsOk() throws Exception {
            List<RoleDTO> roleDTOList = Collections.singletonList(
                    createRoleDTOWithId(testRoleId, "TEST_ROLE"));
            Page<RoleDTO> roleDTOPage = new PageImpl<>(roleDTOList);

            when(roleService.findAll(any(), any())).thenReturn(PageResponse.from(roleDTOPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").exists());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 and support pagination OK")
        void getAllRoles_WithPagination_ReturnsOk() throws Exception {
            List<RoleDTO> roleDTOList = Collections.singletonList(
                    createRoleDTOWithId(testRoleId, "PAGED_ROLE"));
            Page<RoleDTO> roleDTOPage = new PageImpl<>(roleDTOList);

            when(roleService.findAll(any(), any())).thenReturn(PageResponse.from(roleDTOPage));

            mockMvc.perform(get(BASE_API_PATH + "?page=0&size=10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].name").value("PAGED_ROLE"))
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
        void getAllRoles_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/roles")
    class CreateRole {

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 201 Created with role data")
        void createRole_ReturnsCreated() throws Exception {
            RoleDTO createdRoleDTO = createRoleDTOWithId(testRoleId, "NEW_ROLE");

            when(roleService.create(any(RoleDTO.class))).thenReturn(createdRoleDTO);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRoleDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(testRoleId.toString()))
                    .andExpect(jsonPath("$.data.name").value("NEW_ROLE"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void createRole_WithValidationError_ReturnsBadRequest() throws Exception {
            RoleDTO invalidDTO = new RoleDTO();
            invalidDTO.setName("");

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void createRole_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRoleDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/roles/{id}")
    class GetRoleById {

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 200 OK with role data")
        void getRole_ReturnsOk() throws Exception {
            when(roleService.get(testRoleId)).thenReturn(
                    createRoleDTOWithId(testRoleId, "TEST_ROLE"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testRoleId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("TEST_ROLE"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 404 Not Found when role doesn't exist")
        void getRole_NotFound_ReturnsNotFound() throws Exception {
            when(roleService.get(testRoleId))
                    .thenThrow(new EntityNotFoundException("Role not found"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testRoleId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void getRoleById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH + "/" + testRoleId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/roles/{id}")
    class UpdateRole {

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 200 OK with updated role data")
        void updateRole_ReturnsOk() throws Exception {
            RoleDTO updatedDTO = createRoleDTOWithId(testRoleId, "UPDATED_ROLE");

            when(roleService.update(eq(testRoleId), any(RoleDTO.class)))
                    .thenReturn(updatedDTO);

            mockMvc.perform(patch(BASE_API_PATH + "/" + testRoleId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRoleDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(testRoleId.toString()))
                    .andExpect(jsonPath("$.data.name").value("UPDATED_ROLE"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 404 Not Found when role doesn't exist")
        void updateRole_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Role not found"))
                    .when(roleService)
                    .update(eq(testRoleId), any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + testRoleId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRoleDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void updateRole_WithValidationError_ReturnsBadRequest() throws Exception {
            RoleDTO invalidDTO = new RoleDTO();
            invalidDTO.setName("");

            mockMvc.perform(patch(BASE_API_PATH + "/" + testRoleId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void updateRole_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(patch(BASE_API_PATH + "/" + testRoleId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRoleDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/roles/{id}")
    class DeleteRole {

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 204 No Content on successful deletion")
        void deleteRole_ReturnsOk() throws Exception {
            doNothing().when(roleService).delete(testRoleId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testRoleId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 404 Not Found when role doesn't exist")
        void deleteRole_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Role not found"))
                    .when(roleService).delete(testRoleId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testRoleId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void deleteRole_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/" + testRoleId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
