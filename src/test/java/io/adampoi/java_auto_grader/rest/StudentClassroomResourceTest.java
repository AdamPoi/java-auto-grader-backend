package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.StudentClassroomDTO;
import io.adampoi.java_auto_grader.service.StudentClassroomService;
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
public class StudentClassroomResourceTest {

    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private StudentClassroomService studentClassroomService;

    @Test
    @WithMockUser(authorities = {"STUDENT_CLASSROOM:LIST"})
    public void getAllStudentClassrooms_ReturnsOk() throws Exception {
        StudentClassroomDTO studentClassroomDTO = new StudentClassroomDTO();
        studentClassroomDTO.setId(UUID.randomUUID());
        studentClassroomDTO.setIsActive(true);

        List<StudentClassroomDTO> studentClassroomDTOList = Collections.singletonList(studentClassroomDTO);
        Page<StudentClassroomDTO> studentClassroomDTOPage = new PageImpl<>(studentClassroomDTOList);

        when(studentClassroomService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(studentClassroomDTOPage); // Note: StudentClassroomService.findAll now returns
        // Page<StudentClassroomDTO>

        mockMvc.perform(get("/api/studentClassrooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"STUDENT_CLASSROOM:CREATE"})
    public void createStudentClassroom_ReturnsCreated() throws Exception {
        StudentClassroomDTO studentClassroomDTO = new StudentClassroomDTO();
        studentClassroomDTO.setIsActive(true);
        studentClassroomDTO.setStudent(UUID.randomUUID());
        studentClassroomDTO.setClassroom(UUID.randomUUID());

        StudentClassroomDTO createdStudentClassroomDTO = new StudentClassroomDTO();
        createdStudentClassroomDTO.setId(UUID.randomUUID());
        createdStudentClassroomDTO.setIsActive(true);
        createdStudentClassroomDTO.setStudent(UUID.randomUUID());
        createdStudentClassroomDTO.setClassroom(UUID.randomUUID());

        when(studentClassroomService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdStudentClassroomDTO);

        mockMvc.perform(post("/api/studentClassrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentClassroomDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.student").exists())
                .andExpect(jsonPath("$.data.classroom").exists());
    }

    @Test
    @WithMockUser(authorities = {"STUDENT_CLASSROOM:READ"})
    public void getStudentClassroom_ReturnsOk() throws Exception {
        StudentClassroomDTO studentClassroomDTO = new StudentClassroomDTO();
        studentClassroomDTO.setId(UUID.randomUUID());
        studentClassroomDTO.setIsActive(false);

        when(studentClassroomService.get(studentClassroomDTO.getId())).thenReturn(studentClassroomDTO);

        mockMvc.perform(get("/api/studentClassrooms/" + studentClassroomDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @WithMockUser(authorities = {"STUDENT_CLASSROOM:UPDATE"})
    public void updateStudentClassroom_ReturnsOk() throws Exception {
        UUID studentClassroomId = UUID.randomUUID();
        StudentClassroomDTO studentClassroomDTO = new StudentClassroomDTO();
        studentClassroomDTO.setIsActive(false);

        StudentClassroomDTO updatedStudentClassroomDTO = new StudentClassroomDTO();
        updatedStudentClassroomDTO.setId(studentClassroomId);
        updatedStudentClassroomDTO.setIsActive(false);
        updatedStudentClassroomDTO.setStudent(UUID.randomUUID());
        updatedStudentClassroomDTO.setClassroom(UUID.randomUUID());

        when(studentClassroomService.update(eq(studentClassroomId), any(StudentClassroomDTO.class)))
                .thenReturn(updatedStudentClassroomDTO);

        mockMvc.perform(patch("/api/studentClassrooms/" + studentClassroomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentClassroomDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(studentClassroomId.toString()))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @WithMockUser(authorities = {"STUDENT_CLASSROOM:DELETE"})
    public void deleteStudentClassroom_ReturnsOk() throws Exception {
        UUID studentClassroomId = UUID.randomUUID();
        doNothing().when(studentClassroomService).delete(studentClassroomId);

        mockMvc.perform(delete("/api/studentClassrooms/" + studentClassroomId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"STUDENT_CLASSROOM:READ"})
    public void getStudentClassroom_NotFound_ReturnsNotFound() throws Exception {
        UUID studentClassroomId = UUID.randomUUID();
        when(studentClassroomService.get(studentClassroomId))
                .thenThrow(new EntityNotFoundException("StudentClassroom not found"));

        mockMvc.perform(get("/api/studentClassrooms/" + studentClassroomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"STUDENT_CLASSROOM:UPDATE"})
    public void updateStudentClassroom_NotFound_ReturnsNotFound() throws Exception {
        UUID studentClassroomId = UUID.randomUUID();
        StudentClassroomDTO studentClassroomDTO = new StudentClassroomDTO();
        studentClassroomDTO.setIsActive(true);

        doThrow(new EntityNotFoundException("StudentClassroom not found"))
                .when(studentClassroomService)
                .update(org.mockito.ArgumentMatchers.eq(studentClassroomId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/studentClassrooms/" + studentClassroomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentClassroomDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"STUDENT_CLASSROOM:DELETE"})
    public void deleteStudentClassroom_NotFound_ReturnsNotFound() throws Exception {
        UUID studentClassroomId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("StudentClassroom not found")).when(studentClassroomService)
                .delete(studentClassroomId);

        mockMvc.perform(delete("/api/studentClassrooms/" + studentClassroomId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllStudentClassrooms_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/studentClassrooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getStudentClassroomById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID studentClassroomId = UUID.randomUUID();
        mockMvc.perform(get("/api/studentClassrooms/" + studentClassroomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createStudentClassroom_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        StudentClassroomDTO studentClassroomDTO = new StudentClassroomDTO();
        studentClassroomDTO.setIsActive(true);
        studentClassroomDTO.setStudent(UUID.randomUUID());
        studentClassroomDTO.setClassroom(UUID.randomUUID());

        mockMvc.perform(post("/api/studentClassrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentClassroomDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateStudentClassroom_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID studentClassroomId = UUID.randomUUID();
        StudentClassroomDTO studentClassroomDTO = new StudentClassroomDTO();
        studentClassroomDTO.setIsActive(false);

        doThrow(new EntityNotFoundException("StudentClassroom not found"))
                .when(studentClassroomService)
                .update(org.mockito.ArgumentMatchers.eq(studentClassroomId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/studentClassrooms/" + studentClassroomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentClassroomDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteStudentClassroom_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID studentClassroomId = UUID.randomUUID();

        mockMvc.perform(delete("/api/studentClassrooms/" + studentClassroomId))
                .andExpect(status().isUnauthorized());
    }

    // Validation tests for StudentClassroomDTO would be needed here, similar to
    // UserResourceTest
    // @Test
    // @WithMockUser(authorities = {"STUDENT_CLASSROOM:CREATE"})
    // public void createStudentClassroom_WithValidationError_ReturnsBadRequest()
    // throws Exception {
    // StudentClassroomDTO studentClassroomDTO = new StudentClassroomDTO();
    // studentClassroomDTO.setStudent(null); // Example validation error

    // mockMvc.perform(post("/api/studentClassrooms")
    // .contentType(MediaType.APPLICATION_JSON)
    // .content(objectMapper.writeValueAsString(studentClassroomDTO)))
    // .andExpect(status().isBadRequest())
    // .andExpect(jsonPath("$.error.message").value("Validation failed"))
    // .andExpect(jsonPath("$.error.fieldErrors").isArray());
    // }

    // @Test
    // @WithMockUser(authorities = {"STUDENT_CLASSROOM:UPDATE"})
    // public void updateStudentClassroom_WithValidationError_ReturnsBadRequest()
    // throws Exception {
    // UUID studentClassroomId = UUID.randomUUID();
    // StudentClassroomDTO studentClassroomDTO = new StudentClassroomDTO();
    // studentClassroomDTO.setStudent(null); // Example validation error

    // mockMvc.perform(patch("/api/studentClassrooms/" + studentClassroomId)
    // .contentType(MediaType.APPLICATION_JSON)
    // .content(objectMapper.writeValueAsString(studentClassroomDTO)))
    // .andExpect(status().isBadRequest())
    // .andExpect(jsonPath("$.error.message").value("Validation failed"))
    // .andExpect(jsonPath("$.error.fieldErrors").isArray());
    // }
}
