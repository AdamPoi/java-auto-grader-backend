package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.CourseDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.CourseService;
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

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.adampoi.java_auto_grader.model.dto.CourseDTO.builder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CourseResourceTest {

    private static final String BASE_API_PATH = "/api/courses";
    private static final String AUTHORITY_LIST = "COURSE:LIST";
    private static final String AUTHORITY_CREATE = "COURSE:CREATE";
    private static final String AUTHORITY_READ = "COURSE:READ";
    private static final String AUTHORITY_UPDATE = "COURSE:UPDATE";
    private static final String AUTHORITY_DELETE = "COURSE:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CourseService courseService;
    private UUID testCourseId;
    private CourseDTO testCourseDTO;

    @BeforeEach
    void setUp() {
        testCourseId = UUID.randomUUID();
        testCourseDTO = createCourseDTO("TEST101", "Test Course");
    }

    private CourseDTO createCourseDTO(String code, String name) {
        return builder()
                .code(code)
                .name(name)
                .description("Description for " + name)
                .isActive(true)
                .enrollmentStartDate(OffsetDateTime.now())
                .enrollmentEndDate(OffsetDateTime.now().plusMonths(6))
                .createdByTeacher(UUID.randomUUID())
                .build();
    }

    private CourseDTO createCourseDTOWithId(UUID id, String code, String name) {
        return builder()
                .id(id)
                .code(code)
                .name(name)
                .description("Description for " + name)
                .isActive(true)
                .enrollmentStartDate(OffsetDateTime.now())
                .enrollmentEndDate(OffsetDateTime.now().plusMonths(6))
                .createdByTeacher(UUID.randomUUID())
                .build();
    }

    @Nested
    @DisplayName("GET /api/courses")
    class GetAllCourses {
        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 OK with courses")
        void shouldReturnCoursesWhenAuthorized() throws Exception {
            List<CourseDTO> courseDTOList = Collections.singletonList(
                    createCourseDTOWithId(testCourseId, "TEST101", "Test Course"));
            Page<CourseDTO> courseDTOPage = new PageImpl<>(courseDTOList);

            when(courseService.findAll(any(), any())).thenReturn(PageResponse.from(courseDTOPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").exists());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 and support pagination")
        void shouldReturnPaginatedCoursesWhenRequested() throws Exception {
            List<CourseDTO> courseDTOList = Collections.singletonList(
                    createCourseDTOWithId(testCourseId, "PAG101", "Paged Course"));
            Page<CourseDTO> courseDTOPage = new PageImpl<>(courseDTOList);

            when(courseService.findAll(any(), any())).thenReturn(PageResponse.from(courseDTOPage));

            mockMvc.perform(get(BASE_API_PATH + "?page=0&size=10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].name").value("Paged Course"))
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
        void shouldReturnUnauthorizedWhenNotAuthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/courses")
    class CreateCourse {
        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 201 Created with course data")
        void createCourse_ReturnsCreated() throws Exception {
            CourseDTO createdCourseDTO = createCourseDTOWithId(testCourseId,
                    "NEW101", "New Course");

            when(courseService.create(any(CourseDTO.class))).thenReturn(createdCourseDTO);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCourseDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(testCourseId.toString()))
                    .andExpect(jsonPath("$.data.code").value("NEW101"))
                    .andExpect(jsonPath("$.data.name").value("New Course"))
                    .andExpect(jsonPath("$.data.isActive").value(true))
                    .andExpect(jsonPath("$.data.createdByTeacher").exists());

            verify(courseService, times(1)).create(any(CourseDTO.class));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void createCourse_WithValidationError_ReturnsBadRequest() throws Exception {
            CourseDTO invalidDTO = builder()
                    .code("")
                    .name("")
                    .build();

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
        void createCourse_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCourseDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/courses/{id}")
    class GetCourseById {
        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 200 OK with course data")
        void getCourse_ReturnsOk() throws Exception {
            when(courseService.get(testCourseId)).thenReturn(
                    createCourseDTOWithId(testCourseId, "TEST101", "Test Course"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testCourseId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Test Course"));

            verify(courseService, times(1)).get(testCourseId);
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 404 Not Found when course doesn't exist")
        void getCourse_NotFound_ReturnsNotFound() throws Exception {
            when(courseService.get(testCourseId))
                    .thenThrow(new EntityNotFoundException("Course not found"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testCourseId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void getCourseById_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH + "/" + testCourseId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/courses/{id}")
    class UpdateCourse {
        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 200 OK with updated course data")
        void updateCourse_ReturnsOk() throws Exception {
            CourseDTO updatedDTO = createCourseDTOWithId(testCourseId, "UPD101", "Updated Course");
            updatedDTO.setIsActive(false);
            updatedDTO.setEnrollmentEndDate(OffsetDateTime.now().plusYears(1));

            when(courseService.update(eq(testCourseId), any(CourseDTO.class)))
                    .thenReturn(updatedDTO);

            mockMvc.perform(patch(BASE_API_PATH + "/" + testCourseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedDTO))) // Use updatedDTO here
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(testCourseId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Updated Course"))
                    .andExpect(jsonPath("$.data.isActive").value(false));

            verify(courseService, times(1)).update(eq(testCourseId), any(CourseDTO.class));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 404 Not Found when course doesn't exist")
        void updateCourse_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Course not found"))
                    .when(courseService)
                    .update(eq(testCourseId), any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + testCourseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCourseDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void updateCourse_WithValidationError_ReturnsBadRequest() throws Exception {
            CourseDTO invalidDTO = builder()
                    .name("")
                    .build();

            mockMvc.perform(patch(BASE_API_PATH + "/" + testCourseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void updateCourse_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(patch(BASE_API_PATH + "/" + testCourseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCourseDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/courses/{id}")
    class DeleteCourse {
        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 204 No Content on successful deletion")
        void deleteCourse_ReturnsOk() throws Exception {
            doNothing().when(courseService).delete(testCourseId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testCourseId))
                    .andExpect(status().isNoContent());

            verify(courseService, times(1)).delete(testCourseId);
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 404 Not Found when course doesn't exist")
        void deleteCourse_NotFound_ReturnsNotFound() throws Exception {
            doThrow(new EntityNotFoundException("Course not found"))
                    .when(courseService).delete(testCourseId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testCourseId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void deleteCourse_WithNoAuthority_ReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/" + testCourseId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
