package io.adampoi.java_auto_grader.rest;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/api/permissions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Permissions")
public class PermissionResource {

        private final PermissionService permissionService;

        public PermissionResource(final PermissionService permissionService) {
                this.permissionService = permissionService;
        }

        @PreAuthorize("hasAuthority('PERMISSION:LIST')")
        @GetMapping
        @Operation(summary = "Get Permission", description = "Get all permissions with pagination and filtering capabilities")
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
        @Operation(summary = "Get Permission", description = "Get a permission by its ID")
        public ApiSuccessResponse<PermissionDTO> getPermission(
                        @PathVariable("permissionId") UUID permissionId) {
                return ApiSuccessResponse.<PermissionDTO>builder()
                                .data(permissionService.get(permissionId))
                                .statusCode(HttpStatus.OK)
                                .build();
        }

        @PreAuthorize("hasAuthority('PERMISSION:CREATE')")
        @PostMapping
        @ApiResponse(responseCode = "201")
        @Operation(summary = "Create Permission", description = "Create a new permission")
        public ApiSuccessResponse<PermissionDTO> createPermission(
                        @RequestBody @Validated(PermissionDTO.CreateGroup.class) PermissionDTO permissionDTO) {
                PermissionDTO createdPermission = permissionService.create(permissionDTO);
                return ApiSuccessResponse.<PermissionDTO>builder()
                                .data(createdPermission)
                                .statusCode(HttpStatus.CREATED)
                                .build();
        }

        @PreAuthorize("hasAuthority('PERMISSION:UPDATE')")
        @PatchMapping("/{permissionId}")
        @ApiResponse(responseCode = "200")
        @Operation(summary = "Update Permission", description = "Update an existing permission")
        public ApiSuccessResponse<PermissionDTO> updatePermission(
                        @PathVariable("permissionId") UUID permissionId,
                        @RequestBody @Validated(PermissionDTO.UpdateGroup.class) PermissionDTO permissionDTO) {
                PermissionDTO updatedPermission = permissionService.update(permissionId, permissionDTO);
                return ApiSuccessResponse.<PermissionDTO>builder()
                                .data(updatedPermission)
                                .statusCode(HttpStatus.OK)
                                .build();
        }

        @PreAuthorize("hasAuthority('PERMISSION:DELETE')")
        @DeleteMapping("/{permissionId}")
        @Operation(summary = "Delete Permission", description = "Delete an existing permission")
        public ApiSuccessResponse<Void> deletePermission(
                        @PathVariable("permissionId") UUID permissionId) {
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
