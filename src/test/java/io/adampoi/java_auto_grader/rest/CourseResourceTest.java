package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.CourseDTO;
import io.adampoi.java_auto_grader.service.CourseService;
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
public class CourseResourceTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CourseService courseService;

    @Test
    @WithMockUser(authorities = {"COURSE:LIST"})
    public void getAllCourses_ReturnsOk() throws Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(UUID.randomUUID());
        courseDTO.setName("Test Course");

        List<CourseDTO> courseDTOList = Collections.singletonList(courseDTO);
        Page<CourseDTO> courseDTOPage = new PageImpl<>(courseDTOList);

        when(courseService.findAll(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(courseDTOPage);

        mockMvc.perform(get("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"COURSE:CREATE"})
    public void createCourse_ReturnsCreated() throws Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setCode("NEW101");
        courseDTO.setName("New Course");
        courseDTO.setDescription("Description");
        courseDTO.setIsActive(true);
        courseDTO.setEnrollmentStartDate(OffsetDateTime.now());
        courseDTO.setEnrollmentEndDate(OffsetDateTime.now().plusMonths(6));
        courseDTO.setCreatedByTeacher(UUID.randomUUID());

        CourseDTO createdCourseDTO = new CourseDTO();
        createdCourseDTO.setId(UUID.randomUUID());
        createdCourseDTO.setCode("NEW101");
        createdCourseDTO.setName("New Course");
        createdCourseDTO.setDescription("Description");
        createdCourseDTO.setIsActive(true);
        createdCourseDTO.setEnrollmentStartDate(OffsetDateTime.now());
        createdCourseDTO.setEnrollmentEndDate(OffsetDateTime.now().plusMonths(6));
        createdCourseDTO.setCreatedByTeacher(UUID.randomUUID());

        when(courseService.create(org.mockito.ArgumentMatchers.any())).thenReturn(createdCourseDTO);

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.code").value("NEW101"))
                .andExpect(jsonPath("$.data.name").value("New Course"))
                .andExpect(jsonPath("$.data.description").value("Description"))
                .andExpect(jsonPath("$.data.isActive").value(true))
                .andExpect(jsonPath("$.data.createdByTeacher").exists());
    }

    @Test
    @WithMockUser(authorities = {"COURSE:READ"})
    public void getCourse_ReturnsOk() throws Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setId(UUID.randomUUID());
        courseDTO.setName("Test Course");

        when(courseService.get(courseDTO.getId())).thenReturn(courseDTO);

        mockMvc.perform(get("/api/courses/" + courseDTO.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(courseDTO.getName()));
    }

    @Test
    @WithMockUser(authorities = {"COURSE:UPDATE"})
    public void updateCourse_ReturnsOk() throws Exception {
        UUID courseId = UUID.randomUUID();
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("Updated Course");
        courseDTO.setDescription("Updated Description");

        CourseDTO updatedCourseDTO = new CourseDTO();
        updatedCourseDTO.setId(courseId);
        updatedCourseDTO.setCode("UPDATED101");
        updatedCourseDTO.setName("Updated Course");
        updatedCourseDTO.setDescription("Updated Description");
        updatedCourseDTO.setIsActive(false);
        updatedCourseDTO.setEnrollmentStartDate(OffsetDateTime.now());
        updatedCourseDTO.setEnrollmentEndDate(OffsetDateTime.now().plusYears(1));
        updatedCourseDTO.setCreatedByTeacher(UUID.randomUUID());

        when(courseService.update(eq(courseId), any(CourseDTO.class))).thenReturn(updatedCourseDTO);

        mockMvc.perform(patch("/api/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(courseId.toString()))
                .andExpect(jsonPath("$.data.name").value("Updated Course"))
                .andExpect(jsonPath("$.data.description").value("Updated Description"));
    }

    @Test
    @WithMockUser(authorities = {"COURSE:DELETE"})
    public void deleteCourse_ReturnsOk() throws Exception {
        UUID courseId = UUID.randomUUID();
        doNothing().when(courseService).delete(courseId);

        mockMvc.perform(delete("/api/courses/" + courseId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"COURSE:READ"})
    public void getCourse_NotFound_ReturnsNotFound() throws Exception {
        UUID courseId = UUID.randomUUID();
        when(courseService.get(courseId)).thenThrow(new EntityNotFoundException("Course not found"));

        mockMvc.perform(get("/api/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"COURSE:UPDATE"})
    public void updateCourse_NotFound_ReturnsNotFound() throws Exception {
        UUID courseId = UUID.randomUUID();
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("Updated Course");

        doThrow(new EntityNotFoundException("Course not found"))
                .when(courseService)
                .update(org.mockito.ArgumentMatchers.eq(courseId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"COURSE:DELETE"})
    public void deleteCourse_NotFound_ReturnsNotFound() throws Exception {
        UUID courseId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Course not found")).when(courseService).delete(courseId);

        mockMvc.perform(delete("/api/courses/" + courseId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getAllCourses_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void getCourseById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID courseId = UUID.randomUUID();
        mockMvc.perform(get("/api/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void createCourse_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("New Course");
        courseDTO.setCode("C101");
        courseDTO.setCreatedByTeacher(UUID.randomUUID());

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void updateCourse_WithNoAuthority_ReturnsNotFound() throws Exception {
        UUID courseId = UUID.randomUUID();
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("Updated Course");

        doThrow(new EntityNotFoundException("Course not found"))
                .when(courseService)
                .update(org.mockito.ArgumentMatchers.eq(courseId), org.mockito.ArgumentMatchers.any());

        mockMvc.perform(patch("/api/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {})
    public void deleteCourse_WithNoAuthority_ReturnsUnauthorized() throws Exception {
        UUID courseId = UUID.randomUUID();

        mockMvc.perform(delete("/api/courses/" + courseId))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(authorities = {"COURSE:CREATE"})
    public void createCourse_WithValidationError_ReturnsBadRequest() throws
            Exception {
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("");

        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    @WithMockUser(authorities = {"COURSE:UPDATE"})
    public void updateCourse_WithValidationError_ReturnsBadRequest() throws
            Exception {
        UUID courseId = UUID.randomUUID();
        CourseDTO courseDTO = new CourseDTO();
        courseDTO.setName("");

        mockMvc.perform(patch("/api/courses/" + courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }
}
