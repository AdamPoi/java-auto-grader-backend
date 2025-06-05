package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.filter.PermissionFilterDef;
import io.adampoi.java_auto_grader.model.dto.PermissionDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.service.PermissionService;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.annotations.QFParam;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        value = "/api/permissions",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Tag(name = "Permissions")
public class PermissionResource {

    private final PermissionService permissionService;

    public PermissionResource(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PreAuthorize("hasAuthority('PERMISSION:LIST')")
    @GetMapping
    @Operation(summary = "Get Permission",
            description = "Get all permissions with pagination and filtering capabilities")
    public ApiSuccessResponse<PageResponse<PermissionDTO>> getAllPermissions(
            @RequestParam(required = false, defaultValue = "") @QFParam(PermissionFilterDef.class) QueryFilter<Permission> filter,
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable params) {
        return ApiSuccessResponse.<PageResponse<PermissionDTO>>builder()
                .data(permissionService.findAll(filter, params))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('PERMISSION:READ')")
    @GetMapping("/{permissionId}")
    @Operation(summary = "Get Permission",
            description = "Get a permission by its ID")
    public ApiSuccessResponse<PermissionDTO> getPermission(
            @PathVariable("permissionId") UUID permissionId
    ) {
        return ApiSuccessResponse.<PermissionDTO>builder()
                .data(permissionService.get(permissionId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('PERMISSION:CREATE')")
    @PostMapping
    @Operation(summary = "Create Permission",
            description = "Create a new permission")
    public ApiSuccessResponse<UUID> createPermission(
            @RequestBody @Validated(PermissionDTO.CreateGroup.class) PermissionDTO permissionDTO
    ) {
        UUID createdPermissionId = permissionService.create(permissionDTO);
        return ApiSuccessResponse.<UUID>builder()
                .data(createdPermissionId)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PreAuthorize("hasAuthority('PERMISSION:UPDATE')")
    @PatchMapping("/{permissionId}")
    @Operation(summary = "Update Permission",
            description = "Update an existing permission")
    public ApiSuccessResponse<UUID> updatePermission(
            @PathVariable("permissionId") UUID permissionId,
            @RequestBody @Validated(PermissionDTO.UpdateGroup.class) PermissionDTO permissionDTO
    ) {
        permissionService.update(permissionId, permissionDTO);
        return ApiSuccessResponse.<UUID>builder()
                .data(permissionId)
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PreAuthorize("hasAuthority('PERMISSION:DELETE')")
    @DeleteMapping("/{permissionId}")
    @Operation(summary = "Delete Permission",
            description = "Delete an existing permission")
    public ApiSuccessResponse<Void> deletePermission(
            @PathVariable("permissionId") UUID permissionId
    ) {
        ReferencedWarning referencedWarning = permissionService.getReferencedWarning(permissionId);
        if (referencedWarning != null) {
            throw new ReferencedException(referencedWarning);
        }
        permissionService.delete(permissionId);
        return ApiSuccessResponse.<Void>builder()
                .statusCode(HttpStatus.NO_CONTENT)
                .build();
    }
}
