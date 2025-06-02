package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.model.dto.RoleDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.service.RoleService;
import io.adampoi.java_auto_grader.util.CustomCollectors;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping(value = "/api/roles", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleResource {

    private final RoleService roleService;
    private final PermissionRepository permissionRepository;

    public RoleResource(final RoleService roleService, final PermissionRepository permissionRepository) {
        this.roleService = roleService;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('ROLE:READ')")
    public ApiSuccessResponse<Page<RoleDTO>> getAllRoles(Pageable pageable, @RequestParam Map<String, RoleDTO> params) {

        return ApiSuccessResponse.<Page<RoleDTO>>builder()
                .data(roleService.findAll(pageable, params))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE:READ')")
    public ApiSuccessResponse<RoleDTO> getRole(@PathVariable(name = "roleId") final UUID roleId) {

        return ApiSuccessResponse.<RoleDTO>builder()
                .data(roleService.get(roleId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('ROLE:CREATE')")
    public ApiSuccessResponse<RoleDTO> createRole(
            @RequestBody @Validated(RoleDTO.CreateGroup.class) final RoleDTO roleDTO) {
        final RoleDTO createdRole = roleService.create(roleDTO);
        return ApiSuccessResponse.<RoleDTO>builder()
                .data(createdRole)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PatchMapping("/{roleId}")
    @PreAuthorize("hasAuthority('ROLE:UPDATE')")
    public ApiSuccessResponse<RoleDTO> updateRole(@PathVariable(name = "roleId") final UUID roleId,
                                                  @RequestBody @Validated(RoleDTO.UpdateGroup.class) RoleDTO roleDTO) {
        final RoleDTO updatedRole = roleService.update(roleId, roleDTO);
        return ApiSuccessResponse.<RoleDTO>builder()
                .data(updatedRole)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{roleId}")
    @ApiResponse(responseCode = "204")
    @PreAuthorize("hasAuthority('ROLE:DELETE')")
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
