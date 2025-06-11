package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.ClassroomDTO;
import io.adampoi.java_auto_grader.service.ClassroomService;
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
public class ClassroomResourceTest {

    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ClassroomService classroomService;

    @Test
    @WithMockUser(authorities = {"CLASSROOM:LIST"})
    public void getAllClassrooms_ReturnsOk() throws Exception {
        ClassroomDTO classroomDTO = new ClassroomDTO();
        classroomDTO.setId(UUID.randomUUID());
        classroomDTO.setName("Test Classroom");

        List<ClassroomDTO> classroomDTOList = Collections.singletonList(classroomDTO);
        Page<ClassroomDTO> classroomDTOPage = new PageImpl<>(classroomDTOList);

        when(classroomService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(classroomDTOPage); // Note: ClassroomService.findAll now returns Page<ClassroomDTO>

        mockMvc.perform(get("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"CLASSROOM:CREATE"})
    public void createClassroom_ReturnsCreated() throws Exception {
        ClassroomDTO classroomDTO = new ClassroomDTO();
        classroomDTO.setName("New Classroom");
        classroomDTO.setIsActive(true);
        classroomDTO.setEnrollmentStartDate(OffsetDateTime.now());
        classroomDTO.setEnrollmentEndDate(OffsetDateTime.now().plusMonths(3));
        classroomDTO.setCourse(UUID.randomUUID());
        classroomDTO.setTeacher(UUID.randomUUID());

        ClassroomDTO createdClassroomDTO = new ClassroomDTO();
        createdClassroomDTO.setId(UUID.randomUUID());
        createdClassroomDTO.setName("New Classroom");
        createdClassroomDTO.setIsActive(true);
        createdClassroomDTO.setEnrollmentStartDate(OffsetDateTime.now());
        createdClassroomDTO.setEnrollmentEndDate(OffsetDateTime.now().plusMonths(3));
        createdClassroomDTO.setCourse(UUID.randomUUID());
        createdClassroomDTO.setTeacher(UUID.randomUUID());

        when(classroomService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdClassroomDTO);

        mockMvc.perform(post("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classroomDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name").value("New Classroom"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.course").exists())
                .andExpect(jsonPath("$.data.teacher").exists());
    }

    @Test
    @WithMockUser(authorities = {"CLASSROOM:READ"})
    public void getClassroom_ReturnsOk() throws Exception {
        ClassroomDTO classroomDTO = new ClassroomDTO();
        classroomDTO.setId(UUID.randomUUID());
        classroomDTO.setName("Test Classroom");

        when(classroomService.get(classroomDTO.getId())).thenReturn(classroomDTO);

        mockMvc.perform(get("/api/classrooms/" + classroomDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(classroomDTO.getName()));
    }

    @Test
    @WithMockUser(authorities = {"CLASSROOM:UPDATE"})
    public void updateClassroom_ReturnsOk() throws Exception {
        UUID classroomId = UUID.randomUUID();
        ClassroomDTO classroomDTO = new ClassroomDTO();
        classroomDTO.setName("Updated Classroom");
        classroomDTO.setIsActive(false);

        ClassroomDTO updatedClassroomDTO = new ClassroomDTO();
        updatedClassroomDTO.setId(classroomId);
        updatedClassroomDTO.setName("Updated Classroom");
        updatedClassroomDTO.setIsActive(false);
        updatedClassroomDTO.setEnrollmentStartDate(OffsetDateTime.now());
        updatedClassroomDTO.setEnrollmentEndDate(OffsetDateTime.now().plusMonths(6));
        updatedClassroomDTO.setCourse(UUID.randomUUID());
        updatedClassroomDTO.setTeacher(UUID.randomUUID());

        when(classroomService.update(eq(classroomId), any(ClassroomDTO.class))).thenReturn(updatedClassroomDTO);

        mockMvc.perform(patch("/api/classrooms/" + classroomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classroomDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(classroomId.toString()))
                .andExpect(jsonPath("$.data.name").value("Updated Classroom"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @WithMockUser(authorities = {"CLASSROOM:DELETE"})
    public void deleteClassroom_ReturnsOk() throws Exception {
        UUID classroomId = UUID.randomUUID();
        doNothing().when(classroomService).delete(classroomId);

        mockMvc.perform(delete("/api/classrooms/" + classroomId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"CLASSROOM:READ"})
    public void getClassroom_NotFound_ReturnsNotFound() throws Exception {
        UUID classroomId = UUID.randomUUID();
        when(classroomService.get(classroomId)).thenThrow(new EntityNotFoundException("Classroom not found"));

        mockMvc.perform(get("/api/classrooms/" + classroomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"CLASSROOM:UPDATE"})
    public void updateClassroom_NotFound_ReturnsNotFound() throws Exception {
        UUID classroomId = UUID.randomUUID();
        ClassroomDTO classroomDTO = new ClassroomDTO();
        classroomDTO.setName("Updated Classroom");

        doThrow(new EntityNotFoundException("Classroom not found"))
                .when(classroomService)
                .update(org.mockito.ArgumentMatchers.eq(classroomId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/classrooms/" + classroomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classroomDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"CLASSROOM:DELETE"})
    public void deleteClassroom_NotFound_ReturnsNotFound() throws Exception {
        UUID classroomId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Classroom not found")).when(classroomService).delete(classroomId);

        mockMvc.perform(delete("/api/classrooms/" + classroomId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllClassrooms_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getClassroomById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID classroomId = UUID.randomUUID();
        mockMvc.perform(get("/api/classrooms/" + classroomId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createClassroom_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        ClassroomDTO classroomDTO = new ClassroomDTO();
        classroomDTO.setName("New Classroom");
        classroomDTO.setTeacher(UUID.randomUUID());
        classroomDTO.setCourse(UUID.randomUUID());

        mockMvc.perform(post("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classroomDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateClassroom_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID classroomId = UUID.randomUUID();
        ClassroomDTO classroomDTO = new ClassroomDTO();
        classroomDTO.setName("Updated Classroom");

        doThrow(new EntityNotFoundException("Classroom not found"))
                .when(classroomService)
                .update(org.mockito.ArgumentMatchers.eq(classroomId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/classrooms/" + classroomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classroomDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteClassroom_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID classroomId = UUID.randomUUID();

        mockMvc.perform(delete("/api/classrooms/" + classroomId))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(authorities = {"CLASSROOM:CREATE"})
    public void createClassroom_WithValidationError_ReturnsBadRequest() throws
            Exception {
        ClassroomDTO classroomDTO = new ClassroomDTO();
        classroomDTO.setName(""); // Example validation error

        mockMvc.perform(post("/api/classrooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classroomDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"CLASSROOM:UPDATE"})
    public void updateClassroom_WithValidationError_ReturnsBadRequest() throws
            Exception {
        UUID classroomId = UUID.randomUUID();
        ClassroomDTO classroomDTO = new ClassroomDTO();
        classroomDTO.setName(""); // Example validation error

        mockMvc.perform(patch("/api/classrooms/" + classroomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(classroomDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }
}
