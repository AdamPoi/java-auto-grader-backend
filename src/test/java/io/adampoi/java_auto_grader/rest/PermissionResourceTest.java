package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.PermissionDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.PermissionService;
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
public class PermissionResourceTest {

    private static final String BASE_API_PATH = "/api/permissions";
    private static final String AUTHORITY_LIST = "PERMISSION:LIST";
    private static final String AUTHORITY_CREATE = "PERMISSION:CREATE";
    private static final String AUTHORITY_READ = "PERMISSION:READ";
    private static final String AUTHORITY_UPDATE = "PERMISSION:UPDATE";
    private static final String AUTHORITY_DELETE = "PERMISSION:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PermissionService permissionService;
    private UUID testPermissionId;
    private PermissionDTO testPermissionDTO;

    @BeforeEach
    void setUp() {
        testPermissionId = UUID.randomUUID();
        testPermissionDTO = createPermissionDTO("PERMISSION:TEST");
    }

    private PermissionDTO createPermissionDTO(String name) {
        PermissionDTO dto = new PermissionDTO();
        dto.setName(name);
        dto.setDescription("Test description");
        return dto;
    }

    private PermissionDTO createPermissionDTOWithId(UUID id, String name) {
        PermissionDTO dto = createPermissionDTO(name);
        dto.setId(id);
        return dto;
    }

    @Nested
    @DisplayName("GET /api/permissions")
    class GetAllPermissions {
        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 OK with permissions")
        void shouldReturnPermissionsWhenAuthorized() throws Exception {
            List<PermissionDTO> permissionList = Collections.singletonList(
                    createPermissionDTOWithId(testPermissionId, "PERMISSION:TEST"));
            Page<PermissionDTO> permissionPage = new PageImpl<>(permissionList);

            when(permissionService.findAll(any(), any())).thenReturn(PageResponse.from(permissionPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").exists());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void shouldReturnUnauthorizedWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/permissions")
    class CreatePermission {
        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 201 Created with permission data")
        void createPermission_ReturnsCreated() throws Exception {
            PermissionDTO createdDTO = createPermissionDTOWithId(testPermissionId, "PERMISSION:NEW");

            when(permissionService.create(any(PermissionDTO.class))).thenReturn(createdDTO);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testPermissionDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(testPermissionId.toString()))
                    .andExpect(jsonPath("$.data.name").value("PERMISSION:NEW"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void createPermission_WithValidationError_ReturnsBadRequest() throws Exception {
            PermissionDTO invalidDTO = new PermissionDTO();
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
        void createPermission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testPermissionDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/permissions/{id}")
    class GetPermissionById {
        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 200 OK with permission data")
        void getPermission_ReturnsOk() throws Exception {
            when(permissionService.get(testPermissionId)).thenReturn(
                    createPermissionDTOWithId(testPermissionId, "PERMISSION:TEST"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testPermissionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("PERMISSION:TEST"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 404 Not Found when permission doesn't exist")
        void getPermission_NotFound_ReturnsNotFound() throws Exception {
            when(permissionService.get(testPermissionId))
                    .thenThrow(new EntityNotFoundException("Permission not found"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testPermissionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void getPermissionById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH + "/" + testPermissionId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/permissions/{id}")
    class UpdatePermission {
        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 200 OK with updated permission data")
        void updatePermission_ReturnsOk() throws Exception {
            PermissionDTO updatedDTO = createPermissionDTOWithId(testPermissionId, "PERMISSION:UPDATED");

            when(permissionService.update(eq(testPermissionId), any(PermissionDTO.class)))
                    .thenReturn(updatedDTO);

            mockMvc.perform(patch(BASE_API_PATH + "/" + testPermissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testPermissionDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("PERMISSION:UPDATED"));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 404 Not Found when permission doesn't exist")
        void updatePermission_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Permission not found"))
                    .when(permissionService)
                    .update(eq(testPermissionId), any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + testPermissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testPermissionDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void updatePermission_WithValidationError_ReturnsBadRequest() throws Exception {
            PermissionDTO invalidDTO = new PermissionDTO();
            invalidDTO.setName("");

            mockMvc.perform(patch(BASE_API_PATH + "/" + testPermissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void updatePermission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(patch(BASE_API_PATH + "/" + testPermissionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testPermissionDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/permissions/{id}")
    class DeletePermission {
        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 204 No Content on successful deletion")
        void deletePermission_ReturnsNoContent() throws Exception {
            doNothing().when(permissionService).delete(testPermissionId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testPermissionId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 404 Not Found when permission doesn't exist")
        void deletePermission_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Permission not found"))
                    .when(permissionService).delete(testPermissionId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testPermissionId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void deletePermission_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/" + testPermissionId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
