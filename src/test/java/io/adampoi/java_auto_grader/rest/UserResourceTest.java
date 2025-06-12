package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import io.adampoi.java_auto_grader.model.request.UserCreateRequest;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.adampoi.java_auto_grader.model.dto.UserDTO.builder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserResourceTest {

    private static final String BASE_API_PATH = "/api/users";
    private static final String AUTHORITY_LIST = "USER:LIST";
    private static final String AUTHORITY_CREATE = "USER:CREATE";
    private static final String AUTHORITY_READ = "USER:READ";
    private static final String AUTHORITY_UPDATE = "USER:UPDATE";
    private static final String AUTHORITY_DELETE = "USER:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {
        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("Should return 200 OK with users list")
        void getAllUsers_ReturnsOk() throws Exception {
            UserDTO userDTO = builder()
                    .id(UUID.randomUUID())
                    .email("test@example.com")
                    .build();

            List<UserDTO> userDTOList = Collections.singletonList(userDTO);
            Page<UserDTO> userDTOPage = new PageImpl<>(userDTOList);

            when(userService.findAll(any(), any()))
                    .thenReturn(PageResponse.from(userDTOPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].email").value(userDTO.getEmail()));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void getAllUsers_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {
        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("Should return 201 Created with new user")
        void createUser_ReturnsCreated() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail("test@example.com");
            request.setPassword("password");
            request.setFirstName("John");
            request.setLastName("Doe");
            request.setRoles(Arrays.asList("admin", "teacher"));
            request.setIsActive(true);
            request.setPermissions(Collections.emptyList());

            UserDTO createdUser = builder()
                    .id(UUID.randomUUID())
                    .email(request.getEmail())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .roles(request.getRoles())
                    .isActive(request.getIsActive())
                    .permissions(request.getPermissions())
                    .build();

            when(userService.create(any())).thenReturn(createdUser);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.email").value(createdUser.getEmail()))
                    .andExpect(jsonPath("$.data.firstName").value(createdUser.getFirstName()))
                    .andExpect(jsonPath("$.data.lastName").value(createdUser.getLastName()))
                    .andExpect(jsonPath("$.data.roles").isArray())
                    .andExpect(jsonPath("$.data.isActive").value(createdUser.getIsActive()));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void createUser_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail("test@example.com");
            request.setFirstName("John");
            request.setPassword("password");

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("Should return 400 Bad Request for invalid input")
        void createUser_WithValidationError_ReturnsBadRequest() throws Exception {
            UserCreateRequest request = new UserCreateRequest();
            request.setEmail("");
            request.setPassword("");

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserById {
        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("Should return 200 OK with user details")
        void getUser_ReturnsOk() throws Exception {
            UserDTO user = builder()
                    .id(UUID.randomUUID())
                    .email("test@example.com")
                    .build();

            when(userService.get(user.getId())).thenReturn(user);

            mockMvc.perform(get(BASE_API_PATH + "/" + user.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.email").value(user.getEmail()));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void getUserById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            UUID userId = UUID.randomUUID();
            mockMvc.perform(get(BASE_API_PATH + "/" + userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("Should return 404 Not Found for non-existent user")
        void getUser_NotFound_ReturnsNotFound() throws Exception {
            UUID userId = UUID.randomUUID();
            when(userService.get(userId)).thenThrow(new EntityNotFoundException("User not found"));

            mockMvc.perform(get(BASE_API_PATH + "/" + userId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{id}")
    class UpdateUser {
        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("Should return 200 OK with updated user")
        void updateUser_ReturnsOk() throws Exception {
            UUID userId = UUID.randomUUID();
            UserDTO updateRequest = builder()
                    .email("updated@example.com")
                    .firstName("Updated")
                    .build();

            UserDTO updatedUser = builder()
                    .id(userId)
                    .email(updateRequest.getEmail())
                    .firstName(updateRequest.getFirstName())
                    .lastName("")
                    .build();

            when(userService.update(eq(userId), any(UserDTO.class))).thenReturn(updatedUser);

            mockMvc.perform(patch(BASE_API_PATH + "/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(userId.toString()))
                    .andExpect(jsonPath("$.data.email").value(updatedUser.getEmail()))
                    .andExpect(jsonPath("$.data.firstName").value(updatedUser.getFirstName()));
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void updateUser_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            UUID userId = UUID.randomUUID();
            UserDTO updateRequest = new UserDTO();
            updateRequest.setEmail("updated@example.com");

            mockMvc.perform(patch(BASE_API_PATH + "/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("Should return 404 Not Found for non-existent user")
        void updateUser_NotFound_ReturnsNotFound() throws Exception {
            UUID userId = UUID.randomUUID();
            UserDTO updateRequest = new UserDTO();
            updateRequest.setEmail("updated@example.com");

            doThrow(new EntityNotFoundException("User not found"))
                    .when(userService)
                    .update(eq(userId), any(UserDTO.class));

            mockMvc.perform(patch(BASE_API_PATH + "/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("Should return 400 Bad Request for invalid input")
        void updateUser_WithValidationError_ReturnsBadRequest() throws Exception {
            UUID userId = UUID.randomUUID();
            UserDTO updateRequest = new UserDTO();
            updateRequest.setEmail("");
            updateRequest.setFirstName("");

            mockMvc.perform(patch(BASE_API_PATH + "/" + userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {
        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("Should return 204 No Content on successful deletion")
        void deleteUser_ReturnsOk() throws Exception {
            UUID userId = UUID.randomUUID();
            doNothing().when(userService).delete(userId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + userId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("Should return 401 Unauthorized when missing authority")
        void deleteUser_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            UUID userId = UUID.randomUUID();
            mockMvc.perform(delete(BASE_API_PATH + "/" + userId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("Should return 404 Not Found for non-existent user")
        void deleteUser_NotFound_ReturnsNotFound() throws Exception {
            UUID userId = UUID.randomUUID();
            doThrow(new EntityNotFoundException("User not found"))
                    .when(userService).delete(userId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + userId))
                    .andExpect(status().isNotFound());
        }
    }

}