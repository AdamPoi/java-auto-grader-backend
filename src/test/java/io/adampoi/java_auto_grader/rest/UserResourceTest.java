package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.UserDTO;
import io.adampoi.java_auto_grader.repository.RoleRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    @MockitoBean
    private RoleRepository roleRepository;

    @Test
    @WithMockUser(authorities = {"USER:READ"})
    public void getAllUsers_ReturnsOk() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(UUID.randomUUID());
        userDTO.setEmail("test@example.com");

        List<UserDTO> userDTOList = Collections.singletonList(userDTO);
        Page<UserDTO> userDTOPage = new PageImpl<>(userDTOList);

        when(userService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(userDTOPage);

        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"USER:CREATE"})
    public void createUser_ReturnsCreated() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");

        UUID createdUserId = UUID.randomUUID();
        when(userService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdUserId);

        mockMvc.perform(post("/api/users")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated());
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
        userDTO.setId(userId);
        userDTO.setEmail("updated@example.com");
        userDTO.setFirstName("Jane");
        userDTO.setLastName("Smith");

        doNothing().when(userService).update(org.mockito.ArgumentMatchers.eq(userId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk());
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
    public void getAllUsers_WithNoAuthority_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getUserById_WithNoAuthority_ReturnsForbidden() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(get("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createUser_WithNoAuthority_ReturnsForbidden() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isForbidden());
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
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteUser_WithNoAuthority_ReturnsForbidden() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isForbidden());
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