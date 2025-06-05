package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.filter.RoleFilterDef;
import io.adampoi.java_auto_grader.model.dto.RoleDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.service.RoleService;
import io.adampoi.java_auto_grader.util.CustomCollectors;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping(value = "/api/roles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Roles")
public class RoleResource {

    private final RoleService roleService;
    private final PermissionRepository permissionRepository;

    public RoleResource(final RoleService roleService, final PermissionRepository permissionRepository) {
        this.roleService = roleService;
        this.permissionRepository = permissionRepository;
    }

    @PreAuthorize("hasAuthority('ROLE:LIST')")
    @GetMapping
    @Operation(summary = "Get Role",
            description = "Get all roles with pagination and filtering capabilities")
    public ApiSuccessResponse<PageResponse<RoleDTO>> getAllRoles(
            @RequestParam(required = false, defaultValue = "") @QFParam(RoleFilterDef.class) QueryFilter<Role> filter,
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable params) {
        return ApiSuccessResponse.<PageResponse<RoleDTO>>builder()
                .data(roleService.findAll(filter, params))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('ROLE:READ')")
    @GetMapping("/{roleId}")
    @Operation(summary = "Get Role",
            description = "Get role by id")
    public ApiSuccessResponse<RoleDTO> getRole(@PathVariable(name = "roleId") final UUID roleId) {

        return ApiSuccessResponse.<RoleDTO>builder()
                .data(roleService.get(roleId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('ROLE:CREATE')")
    @PostMapping
    @Operation(summary = "Create Role",
            description = "Create a new role")
    public ApiSuccessResponse<RoleDTO> createRole(
            @RequestBody @Validated(RoleDTO.CreateGroup.class) final RoleDTO roleDTO) {
        final RoleDTO createdRole = roleService.create(roleDTO);
        return ApiSuccessResponse.<RoleDTO>builder()
                .data(createdRole)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    @PatchMapping("/{roleId}")
    @Operation(summary = "Update Role",
            description = "Update an existing role")
    public ApiSuccessResponse<RoleDTO> updateRole(@PathVariable(name = "roleId") final UUID roleId,
                                                  @RequestBody @Validated(RoleDTO.UpdateGroup.class) RoleDTO roleDTO) {
        final RoleDTO updatedRole = roleService.update(roleId, roleDTO);
        return ApiSuccessResponse.<RoleDTO>builder()
                .data(updatedRole)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('ROLE:DELETE')")
    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete Role",
            description = "Delete an existing role")
    public ApiSuccessResponse<Void> deleteRole(@PathVariable(name = "roleId") final UUID roleId) {
        final ReferencedWarning referencedWarning = roleService.getReferencedWarning(roleId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        roleService.delete(roleId);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }

    @GetMapping("/permissions")
    @Operation(summary = "Get Role Permissions",
            description = "Get all role permissions")
    public ApiSuccessResponse<Map<UUID, String>> getRolePermissionsValues() {
        Map<UUID, String> permissions = permissionRepository.findAll(Sort.by("permissionId"))
                .stream()
                .collect(CustomCollectors.toSortedMap(Permission::getId, Permission::getName));
        return ApiSuccessResponse.<Map<UUID, String>>builder()
                .data(permissions)
                .statusCode(HttpStatus.OK)
                .build();
    }
}
