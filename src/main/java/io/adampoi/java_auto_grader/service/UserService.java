package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.*;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.*;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Transactional
@Slf4j
public class UserService {


    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final BCryptPasswordEncoder passwordEncoder;


    public UserService(final UserRepository userRepository, final RoleRepository roleRepository,
                       final CourseRepository courseRepository, final ClassroomRepository classroomRepository,
                       final AssignmentRepository assignmentRepository,
                       final SubmissionRepository submissionRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static UserDTO mapToDTO(final User user, final UserDTO userDTO) {
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setNim(user.getNim());
        userDTO.setNip(user.getNip());
        userDTO.setIsActive(user.getIsActive());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setRoles(user.getUserRoles().stream()
                .map(Role::getName)
                .toList());
        return userDTO;
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

        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        mapToEntity(userDTO, user);
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser, new UserDTO());

    }

    public UserDTO update(final UUID userId, final UserDTO userDTO) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        mapToEntity(userDTO, user);
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser, new UserDTO());

    }

    public void delete(final UUID userId) {
        userRepository.deleteById(userId);
    }

    private User mapToEntity(final UserDTO userDTO, final User user) {
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getNim() != null) {
            user.setNim(userDTO.getNim());
        }
        if (userDTO.getNip() != null) {
            user.setNip(userDTO.getNip());
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

        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            Map<String, Role> availableRoles = roleRepository.findAll().stream()
                    .collect(Collectors.toMap(Role::getName, Function.identity()));

            Set<Role> roles = userDTO.getRoles().stream()
                    .map(roleName -> {
                        Role role = availableRoles.get(roleName);
                        if (role == null) {
                            throw new IllegalArgumentException("Role not found: " + roleName);
                        }
                        return role;
                    })
                    .collect(Collectors.toSet());

            user.setUserRoles(roles);
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
