package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.*;
import io.adampoi.java_auto_grader.model.UserDTO;
import io.adampoi.java_auto_grader.repository.*;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final TeacherCourseRepository teacherCourseRepository;
    private final StudentClassroomRepository studentClassroomRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    public UserService(final UserRepository userRepository, final RoleRepository roleRepository,
                       final CourseRepository courseRepository, final ClassroomRepository classroomRepository,
                       final TeacherCourseRepository teacherCourseRepository,
                       final StudentClassroomRepository studentClassroomRepository,
                       final AssignmentRepository assignmentRepository,
                       final SubmissionRepository submissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.teacherCourseRepository = teacherCourseRepository;
        this.studentClassroomRepository = studentClassroomRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
    }

    public Page<UserDTO> findAll(final Pageable pageable, Map<String, UserDTO> params) {
        Specification<User> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(params.get("firstName"))) {
                predicates.add(builder.or(
                        builder.like(root.get("firstName"), "%" + params.get("firstName") + "%"),
                        builder.like(root.get("lastName"), "%" + params.get("lastName") + "%")
                ));
            }
            if (Objects.nonNull(params.get("email"))) {
                predicates.add(builder.like(root.get("email"), "%" + params.get("email") + "%"));
            }

            return query != null ? query.where(predicates.toArray(new Predicate[]{})).getRestriction() : null;
        };
        final Page<User> page = userRepository.findAll(specification, pageable);
        return new PageImpl<>(page.getContent()
                .stream()
                .map(user -> mapToDTO(user, new UserDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
    }


    public UserDTO get(final UUID userId) {
        return userRepository.findById(userId)
                .map(user -> mapToDTO(user, new UserDTO()))
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public UUID create(final UserDTO userDTO) {
        final User user = new User();
        mapToEntity(userDTO, user);
        return userRepository.save(user).getId();
    }

    public void update(final UUID userId, final UserDTO userDTO) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        mapToEntity(userDTO, user);
        userRepository.save(user);
    }

    public void delete(final UUID userId) {
        userRepository.deleteById(userId);
    }

    private UserDTO mapToDTO(final User user, final UserDTO userDTO) {
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setPassword(user.getPassword());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setIsActive(user.getIsActive());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        userDTO.setUserRoles(user.getUserRoles().stream()
                .map(Role::getId)
                .toList());
        return userDTO;
    }

    private User mapToEntity(final UserDTO userDTO, final User user) {
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setIsActive(userDTO.getIsActive());
        user.setCreatedAt(userDTO.getCreatedAt());
        user.setUpdatedAt(userDTO.getUpdatedAt());
        final List<Role> UserRoles = roleRepository.findAllById(
                userDTO.getUserRoles() == null ? List.of() : userDTO.getUserRoles());
        if (UserRoles.size() != (userDTO.getUserRoles() == null ? 0 : userDTO.getUserRoles().size())) {
            throw new NotFoundException("one of User Roles not found");
        }
        user.setUserRoles(new HashSet<>(UserRoles));
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
