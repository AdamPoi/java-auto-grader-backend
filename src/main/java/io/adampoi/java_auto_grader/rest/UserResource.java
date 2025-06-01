package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.model.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.UserDTO;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.service.UserService;
import io.adampoi.java_auto_grader.util.CustomCollectors;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping(value = "/api/users",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
public class UserResource {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserResource(final UserService userService, final RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ApiSuccessResponse<Page<UserDTO>> getAllUsers(Pageable pageable, @RequestParam Map<String, UserDTO> params) {
        return ApiSuccessResponse.<Page<UserDTO>>builder()
                .data(userService.findAll(pageable, params))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{userId}")
    public ApiSuccessResponse<UserDTO> getUser(@PathVariable(name = "userId") final UUID userId) {

        return ApiSuccessResponse.<UserDTO>builder()
                .data(userService.get(userId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    public ApiSuccessResponse<UUID> createUser(@RequestBody @Valid final UserDTO userDTO) {
        final UUID createdUserId = userService.create(userDTO);
        return ApiSuccessResponse.<UUID>builder()
                .data(createdUserId)
                .statusCode(HttpStatus.CREATED)
                .build();

    }

    @PutMapping("/{userId}")
    public ApiSuccessResponse<UUID> updateUser(@PathVariable(name = "userId") final UUID userId,
                                               @RequestBody @Valid final UserDTO userDTO) {
        userService.update(userId, userDTO);
        return ApiSuccessResponse.<UUID>builder()
                .data(userId)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{userId}")
    @ApiResponse(responseCode = "204")
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

    @GetMapping("/userRoleRolesValues")
    public ApiSuccessResponse<Map<UUID, String>> getUserRoleRolesValues() {
        Map<UUID, String> roles = roleRepository.findAll(Sort.by("roleId"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Role::getId, Role::getName));
        return ApiSuccessResponse.<Map<UUID, String>>builder()
                .data(roles)
                .statusCode(HttpStatus.OK)
                .build();
    }


}
