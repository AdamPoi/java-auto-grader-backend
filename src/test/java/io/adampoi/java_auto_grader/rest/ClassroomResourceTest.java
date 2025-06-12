package io.adampoi.java_auto_grader.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.adampoi.java_auto_grader.model.dto.ClassroomDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.ClassroomService;
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

import static io.adampoi.java_auto_grader.model.dto.ClassroomDTO.builder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClassroomResourceTest {

    private static final String BASE_API_PATH = "/api/classrooms";
    private static final String AUTHORITY_LIST = "CLASSROOM:LIST";
    private static final String AUTHORITY_CREATE = "CLASSROOM:CREATE";
    private static final String AUTHORITY_READ = "CLASSROOM:READ";
    private static final String AUTHORITY_UPDATE = "CLASSROOM:UPDATE";
    private static final String AUTHORITY_DELETE = "CLASSROOM:DELETE";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ClassroomService classroomService;
    private UUID testClassroomId;
    private ClassroomDTO testClassroomDTO;

    @BeforeEach
    void setUp() {
        testClassroomId = UUID.randomUUID();
        testClassroomDTO = createClassroomDTO("Test Classroom");
    }

    private ClassroomDTO createClassroomDTO(String name) {
        return builder()
                .name(name)
                .isActive(true)
                .enrollmentStartDate(OffsetDateTime.now())
                .enrollmentEndDate(OffsetDateTime.now().plusMonths(3))
                .course(UUID.randomUUID())
                .teacher(UUID.randomUUID())
                .build();
    }

    private ClassroomDTO createClassroomDTOWithId(UUID id, String name) {
        return builder()
                .id(id)
                .name(name)
                .isActive(true)
                .enrollmentStartDate(OffsetDateTime.now())
                .enrollmentEndDate(OffsetDateTime.now().plusMonths(3))
                .course(UUID.randomUUID())
                .teacher(UUID.randomUUID())
                .build();
    }

    @Nested
    @DisplayName("GET /api/classrooms")
    class GetAllClassrooms {
        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 OK with classrooms")
        void shouldReturnClassroomsWhenAuthorized() throws Exception {
            List<ClassroomDTO> classroomDTOList = Collections.singletonList(
                    createClassroomDTOWithId(testClassroomId, "Test Classroom"));
            Page<ClassroomDTO> classroomDTOPage = new PageImpl<>(classroomDTOList);

            when(classroomService.findAll(any(), any())).thenReturn(PageResponse.from(classroomDTOPage));

            mockMvc.perform(get(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").exists());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_LIST})
        @DisplayName("should return 200 and support pagination")
        void shouldReturnPaginatedClassroomsWhenRequested() throws Exception {
            List<ClassroomDTO> classroomDTOList = Collections.singletonList(
                    createClassroomDTOWithId(testClassroomId, "Paged Classroom"));
            Page<ClassroomDTO> classroomDTOPage = new PageImpl<>(classroomDTOList);

            when(classroomService.findAll(any(), any())).thenReturn(PageResponse.from(classroomDTOPage));

            mockMvc.perform(get(BASE_API_PATH + "?page=0&size=10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].name").value("Paged Classroom"))
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
    @DisplayName("POST /api/classrooms")
    class CreateClassroom {
        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 201 Created with classroom data")
        void shouldReturnCreatedClassroomWhenValidInput() throws Exception {
            ClassroomDTO createdClassroomDTO = createClassroomDTOWithId(testClassroomId,
                    "New Classroom");

            when(classroomService.create(any(ClassroomDTO.class))).thenReturn(createdClassroomDTO);

            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testClassroomDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(testClassroomId.toString()))
                    .andExpect(jsonPath("$.data.name").value("New Classroom"))
                    .andExpect(jsonPath("$.data.isActive").value(true))
                    .andExpect(jsonPath("$.data.course").exists())
                    .andExpect(jsonPath("$.data.teacher").exists());

            verify(classroomService, times(1)).create(any(ClassroomDTO.class));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_CREATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void shouldReturnBadRequestWhenInvalidInput() throws Exception {
            ClassroomDTO invalidDTO = new ClassroomDTO();
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
        void shouldReturnUnauthorizedWhenCreateNotAuthorized() throws Exception {
            mockMvc.perform(post(BASE_API_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testClassroomDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/classrooms/{id}")
    class GetClassroomById {
        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 200 OK with classroom data")
        void shouldReturnClassroomWhenFoundAndAuthorized() throws Exception {
            when(classroomService.get(testClassroomId)).thenReturn(
                    createClassroomDTOWithId(testClassroomId, "Test Classroom"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testClassroomId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value("Test Classroom"));

            verify(classroomService, times(1)).get(testClassroomId);
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_READ})
        @DisplayName("should return 404 Not Found when classroom doesn't exist")
        void shouldReturnNotFoundWhenClassroomNotFound() throws Exception {
            when(classroomService.get(testClassroomId))
                    .thenThrow(new EntityNotFoundException("Classroom not found"));

            mockMvc.perform(get(BASE_API_PATH + "/" + testClassroomId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void shouldReturnUnauthorizedWhenGetByIdNotAuthorized() throws Exception {
            mockMvc.perform(get(BASE_API_PATH + "/" + testClassroomId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PATCH /api/classrooms/{id}")
    class UpdateClassroom {
        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 200 OK with updated classroom data")
        void shouldReturnUpdatedClassroomWhenValidInput() throws Exception {
            ClassroomDTO updatedDTO = createClassroomDTOWithId(testClassroomId, "Updated Classroom");
            updatedDTO.setIsActive(false);
            updatedDTO.setEnrollmentEndDate(OffsetDateTime.now().plusMonths(6));

            when(classroomService.update(eq(testClassroomId), any(ClassroomDTO.class)))
                    .thenReturn(updatedDTO);

            mockMvc.perform(patch(BASE_API_PATH + "/" + testClassroomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(testClassroomId.toString()))
                    .andExpect(jsonPath("$.data.name").value("Updated Classroom"))
                    .andExpect(jsonPath("$.data.isActive").value(false));

            verify(classroomService, times(1)).update(eq(testClassroomId), any(ClassroomDTO.class));
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 404 Not Found when classroom doesn't exist")
        void shouldReturnNotFoundWhenUpdatingNonExistentClassroom() throws Exception {
            doThrow(new EntityNotFoundException("Classroom not found"))
                    .when(classroomService)
                    .update(eq(testClassroomId), any());

            mockMvc.perform(patch(BASE_API_PATH + "/" + testClassroomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testClassroomDTO)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_UPDATE})
        @DisplayName("should return 400 Bad Request for invalid input")
        void shouldReturnBadRequestWhenUpdatingWithInvalidInput() throws Exception {
            ClassroomDTO invalidDTO = new ClassroomDTO();
            invalidDTO.setName("");

            mockMvc.perform(patch(BASE_API_PATH + "/" + testClassroomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("Validation failed"))
                    .andExpect(jsonPath("$.error.fieldErrors").isArray());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void shouldReturnUnauthorizedWhenUpdateNotAuthorized() throws Exception {
            mockMvc.perform(patch(BASE_API_PATH + "/" + testClassroomId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testClassroomDTO)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/classrooms/{id}")
    class DeleteClassroom {
        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 204 No Content on successful deletion")
        void shouldReturnNoContentWhenDeletedSuccessfully() throws Exception {
            doNothing().when(classroomService).delete(testClassroomId);
            verify(classroomService, times(0)).delete(testClassroomId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testClassroomId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(authorities = {AUTHORITY_DELETE})
        @DisplayName("should return 404 Not Found when classroom doesn't exist")
        void shouldReturnNotFoundWhenDeletingNonExistentClassroom() throws Exception {
            doThrow(new EntityNotFoundException("Classroom not found"))
                    .when(classroomService).delete(testClassroomId);

            mockMvc.perform(delete(BASE_API_PATH + "/" + testClassroomId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(authorities = {})
        @DisplayName("should return 401 Unauthorized when no authority")
        void shouldReturnUnauthorizedWhenDeleteNotAuthorized() throws Exception {
            mockMvc.perform(delete(BASE_API_PATH + "/" + testClassroomId))
                    .andExpect(status().isUnauthorized());
        }
    }
}
