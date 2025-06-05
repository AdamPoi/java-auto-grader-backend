package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.*;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.*;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Transactional
@Slf4j
public class UserService {


    private static final ExampleMatcher SEARCH_CONDITIONS_MATCH_ANY = ExampleMatcher
            .matchingAny()
            .withMatcher("birthDate", ExampleMatcher.GenericPropertyMatchers.exact())
            .withMatcher("firstName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
            .withMatcher("lastName", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
            .withIgnorePaths("employeeId", "gender", "hireDate", "salaries", "titles");
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final TeacherCourseRepository teacherCourseRepository;
    private final StudentClassroomRepository studentClassroomRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(final UserRepository userRepository, final RoleRepository roleRepository,
                       final CourseRepository courseRepository, final ClassroomRepository classroomRepository,
                       final TeacherCourseRepository teacherCourseRepository,
                       final StudentClassroomRepository studentClassroomRepository,
                       final AssignmentRepository assignmentRepository,
                       final SubmissionRepository submissionRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.teacherCourseRepository = teacherCourseRepository;
        this.studentClassroomRepository = studentClassroomRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResponse<UserDTO> findAll(QueryFilter<User> filter, Pageable pageable) {
        final Page<User> page = userRepository.findAll(filter, pageable);

        Page<UserDTO> dtoPage = new PageImpl<>(
                page.getContent()
                        .stream()
                        .map(user -> mapToDTO(user, new UserDTO()))
                        .collect(Collectors.toList()),
                pageable,
                page.getTotalElements()
        );
        return PageResponse.from(dtoPage);
    }

    public UserDTO get(final UUID userId) {
        return userRepository.findById(userId)
                .map(user -> mapToDTO(user, new UserDTO()))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }


    public UserDTO create(final UserDTO userDTO) {
        final User user = new User();
        mapToEntity(userDTO, user);
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser, new UserDTO());

    }

    public UserDTO update(final UUID userId, final UserDTO userDTO) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        mapToEntity(userDTO, user);
        User savedUser = userRepository.save(user);
        mapToDTO(savedUser, new UserDTO());
        return mapToDTO(savedUser, new UserDTO());

    }

    public void delete(final UUID userId) {
        userRepository.deleteById(userId);
    }

    private UserDTO mapToDTO(final User user, final UserDTO userDTO) {

        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setIsActive(user.getIsActive());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setRoles(
                user.getUserRoles().stream()
                        .map(Role::getName)
                        .toList());
        return userDTO;
    }

    private User mapToEntity(final UserDTO userDTO, final User user) {
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getIsActive() != null) {
            user.setIsActive(userDTO.getIsActive());
        }
        if (userDTO.getCreatedAt() != null) {
            user.setCreatedAt(userDTO.getCreatedAt());
        }
        if (userDTO.getUpdatedAt() != null) {
            user.setUpdatedAt(userDTO.getUpdatedAt());
        }
        if (userDTO.getUserRoles() != null) {
            final List<Role> UserRoles = roleRepository.findAllById(userDTO.getUserRoles());
            if (UserRoles.size() != userDTO.getUserRoles().size()) {
                throw new NotFoundException("one of User Roles not found");
            }
            user.setUserRoles(new HashSet<>(UserRoles));
        }
        return user;
    }

    public ReferencedWarning getReferencedWarning(final UUID userId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        final Course createdByTeacherCourse = courseRepository.findFirstByCreatedByTeacher(user);
        if (createdByTeacherCourse != null) {
            referencedWarning.setKey("user.course.createdByTeacher.referenced");
            referencedWarning.addParam(createdByTeacherCourse.getId());
            return referencedWarning;
        }
        final Classroom teacherClassroom = classroomRepository.findFirstByTeacher(user);
        if (teacherClassroom != null) {
            referencedWarning.setKey("user.classroom.teacher.referenced");
            referencedWarning.addParam(teacherClassroom.getId());
            return referencedWarning;
        }
        final TeacherCourse teacherTeacherCourse = teacherCourseRepository.findFirstByTeacher(user);
        if (teacherTeacherCourse != null) {
            referencedWarning.setKey("user.teacherCourse.teacher.referenced");
            referencedWarning.addParam(teacherTeacherCourse.getId());
            return referencedWarning;
        }
        final StudentClassroom studentStudentClassroom = studentClassroomRepository.findFirstByStudent(user);
        if (studentStudentClassroom != null) {
            referencedWarning.setKey("user.studentClassroom.student.referenced");
            referencedWarning.addParam(studentStudentClassroom.getId());
            return referencedWarning;
        }
        final Assignment createdByTeacherAssignment = assignmentRepository.findFirstByCreatedByTeacher(user);
        if (createdByTeacherAssignment != null) {
            referencedWarning.setKey("user.assignment.createdByTeacher.referenced");
            referencedWarning.addParam(createdByTeacherAssignment.getId());
            return referencedWarning;
        }
        final Submission studentSubmission = submissionRepository.findFirstByStudent(user);
        if (studentSubmission != null) {
            referencedWarning.setKey("user.submission.student.referenced");
            referencedWarning.addParam(studentSubmission.getId());
            return referencedWarning;
        }
        return null;
    }

}
