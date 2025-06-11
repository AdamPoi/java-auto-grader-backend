package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import io.adampoi.java_auto_grader.model.request.UserCreateRequest;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.UserService;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private UserService userService;


    @Test
    @WithMockUser(authorities = {"USER:LIST"})
    public void getAllUsers_ReturnsOk() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(UUID.randomUUID());
        userDTO.setEmail("test@example.com");

        List<UserDTO> userDTOList = Collections.singletonList(userDTO);
        Page<UserDTO> userDTOPage = new PageImpl<>(userDTOList);

        when(userService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(PageResponse.from(userDTOPage));

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"USER:CREATE"})
    public void createUser_ReturnsCreated() throws Exception {
        UserCreateRequest userDTO = new UserCreateRequest();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");
        userDTO.setRoles(Arrays.asList("admin", "teacher"));
        userDTO.setIsActive(true);
        userDTO.setPermissions(Collections.emptyList());


        UserDTO createdUserDTO = new UserDTO();
        createdUserDTO.setId(UUID.randomUUID());
        createdUserDTO.setEmail("test@example.com");
        createdUserDTO.setFirstName("John");
        createdUserDTO.setLastName("Doe");
        createdUserDTO.setRoles(Arrays.asList("admin", "teacher"));
        createdUserDTO.setIsActive(true);
        createdUserDTO.setPermissions(Collections.emptyList());

        when(userService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdUserDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.roles", hasSize(2)))
                .andExpect(jsonPath("$.data.roles[0]").value("admin"))
                .andExpect(jsonPath("$.data.roles[1]").value("teacher"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.permissions").isEmpty());
        ;
    }

    @Test
    @WithMockUser(authorities = {"USER:READ"})
    public void getUser_ReturnsOk() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(UUID.randomUUID());
        userDTO.setEmail("test@example.com");

        when(userService.get(userDTO.getId())).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/" + userDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(userDTO.getEmail()));
    }

    @Test
    @WithMockUser(authorities = {"USER:UPDATE"})
    public void updateUser_ReturnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("updated@example.com");
        userDTO.setFirstName("Updated");

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setId(userId);
        updatedUserDTO.setEmail("updated@example.com");
        updatedUserDTO.setFirstName("Updated");
        updatedUserDTO.setLastName("");

        when(userService.update(eq(userId), any(UserDTO.class))).thenReturn(updatedUserDTO);

        mockMvc.perform(patch("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId.toString()))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Updated"));
    }

    @Test
    @WithMockUser(authorities = {"USER:DELETE"})
    public void deleteUser_ReturnsOk() throws Exception {
        UUID userId = UUID.randomUUID();
        doNothing().when(userService).delete(userId);

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"USER:READ"})
    public void getUser_NotFound_ReturnsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.get(userId)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"USER:UPDATE"})
    public void updateUser_NotFound_ReturnsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("updated@example.com");

        doThrow(new EntityNotFoundException("User not found"))
                .when(userService).update(org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"USER:DELETE"})
    public void deleteUser_NotFound_ReturnsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("User not found")).when(userService).delete(userId);

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllUsers_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getUserById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(get("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createUser_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UserCreateRequest userDTO = new UserCreateRequest();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");
        userDTO.setFirstName("Test");


        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateUser_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("updated@example.com");

        doThrow(new EntityNotFoundException("User not found"))
                .when(userService).update(org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteUser_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"USER:CREATE"})
    public void createUser_WithValidationError_ReturnsBadRequest() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("");
        userDTO.setPassword("");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"USER:UPDATE"})
    public void updateUser_WithValidationError_ReturnsBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        UserDTO userDTO = new UserDTO();

        userDTO.setEmail("");
        userDTO.setPassword("");
        userDTO.setFirstName("");
        userDTO.setLastName("");

        mockMvc.perform(patch("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }
}