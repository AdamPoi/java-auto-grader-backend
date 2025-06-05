package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.domain.User;
import io.adampoi.java_auto_grader.filter.UserFilterDef;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import io.adampoi.java_auto_grader.model.request.UserCreateRequest;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.service.UserService;
import io.adampoi.java_auto_grader.util.CustomCollectors;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping(value = "/api/users",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users")
public class UserResource {

    private final UserService userService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public UserResource(final UserService userService, final RoleRepository roleRepository, UserRepository userRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('USER:LIST')")
    @GetMapping
    @Operation(summary = "Get User",
            description = "Get all users with pagination and filtering capabilities")
    public ApiSuccessResponse<PageResponse<UserDTO>> getAllUsers(
            @RequestParam(required = false, defaultValue = "") @QFParam(UserFilterDef.class) QueryFilter<User> filter,
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable params) {
        return ApiSuccessResponse.<PageResponse<UserDTO>>builder()
                .data(userService.findAll(filter, params))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('USER:READ')")
    @GetMapping("/{userId}")
    @Operation(summary = "Get User",
            description = "Get a user by id")
    public ApiSuccessResponse<UserDTO> getUser(@PathVariable(name = "userId") final UUID userId) {

        return ApiSuccessResponse.<UserDTO>builder()
                .data(userService.get(userId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('USER:CREATE')")
    @PostMapping
    @Operation(summary = "Create User",
            description = "Create a new user")
    public ApiSuccessResponse<UserDTO> createUser(@RequestBody @Valid UserCreateRequest userDTO) {

        final UserDTO createdUser = userService.create(userDTO);

        return ApiSuccessResponse.<UserDTO>builder()
                .data(createdUser)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @PatchMapping("/{userId}")
    @Operation(summary = "Update User",
            description = "Update an existing user")
    public ApiSuccessResponse<UserDTO> updateUser(@PathVariable(name = "userId") final UUID userId,
                                                  @RequestBody @Validated(UserDTO.UpdateGroup.class) UserDTO userDTO) {
        final UserDTO updatedUser = userService.update(userId, userDTO);
        return ApiSuccessResponse.<UserDTO>builder()
                .data(updatedUser)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('USER:DELETE')")
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete User",
            description = "Delete an existing user")
    public ApiSuccessResponse<Void> deleteUser(@PathVariable(name = "userId") final UUID userId) {
        final ReferencedWarning referencedWarning = userService.getReferencedWarning(userId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        userService.delete(userId);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/roles")
    @Operation(summary = "Get User Roles",
            description = "Get all user roles")
    public ApiSuccessResponse<Map<UUID, String>> getUserRolesValues() {
        Map<UUID, String> roles = roleRepository.findAll(Sort.by("roleId"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Role::getId, Role::getName));
        return ApiSuccessResponse.<Map<UUID, String>>builder()
                .data(roles)
                .statusCode(HttpStatus.OK)
                .build();
    }


}
