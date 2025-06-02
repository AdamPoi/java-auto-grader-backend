package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.model.dto.UserDTO;
import io.adampoi.java_auto_grader.model.request.UserCreateRequest;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping(value = "/api/users",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class UserResource {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserResource(final UserService userService, final RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @PreAuthorize("hasAuthority('USER:READ')")
    @GetMapping
    @ApiResponse(responseCode = "200")
    public ApiSuccessResponse<Page<UserDTO>> getAllUsers(Pageable pageable, @RequestParam Map<String, UserDTO> params) {

        return ApiSuccessResponse.<Page<UserDTO>>builder()
                .data(userService.findAll(pageable, params))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('USER:READ')")
    @GetMapping("/{userId}")
    public ApiSuccessResponse<UserDTO> getUser(@PathVariable(name = "userId") final UUID userId) {

        return ApiSuccessResponse.<UserDTO>builder()
                .data(userService.get(userId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('USER:CREATE')")
    @PostMapping
    @ApiResponse(responseCode = "201")
    public ApiSuccessResponse<UserDTO> createUser(@RequestBody @Valid UserCreateRequest userDTO) {

        final UserDTO createdUser = userService.create(userDTO);

        return ApiSuccessResponse.<UserDTO>builder()
                .data(createdUser)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PreAuthorize("hasAuthority('USER:UPDATE')")
    @PatchMapping("/{userId}")
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

    @GetMapping("/roles")
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
