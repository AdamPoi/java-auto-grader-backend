package io.adampoi.java_auto_grader.rest;

import io.adampoi.java_auto_grader.model.dto.PermissionDTO;
import io.adampoi.java_auto_grader.model.response.ApiSuccessResponse;
import io.adampoi.java_auto_grader.service.PermissionService;
import io.adampoi.java_auto_grader.util.ReferencedException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(
        value = "/api/permissions",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class PermissionResource {

    private final PermissionService permissionService;

    public PermissionResource(final PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @ApiResponse(responseCode = "200")
    @PreAuthorize("hasAuthority('PERMISSION:READ')")
    public ApiSuccessResponse<Page<PermissionDTO>> getAllPermissions(
            Pageable pageable,
            @RequestParam(required = false) Map<String, PermissionDTO> params
    ) {
        return ApiSuccessResponse.<Page<PermissionDTO>>builder()
                .data(permissionService.findAll(pageable, params))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION:READ')")
    public ApiSuccessResponse<PermissionDTO> getPermission(
            @PathVariable("permissionId") UUID permissionId
    ) {
        return ApiSuccessResponse.<PermissionDTO>builder()
                .data(permissionService.get(permissionId))
                .statusCode(HttpStatus.OK)
                .build();
    }

    @PostMapping
    @ApiResponse(responseCode = "201")
    @PreAuthorize("hasAuthority('PERMISSION:CREATE')")
    public ApiSuccessResponse<UUID> createPermission(
            @RequestBody @Validated(PermissionDTO.CreateGroup.class) PermissionDTO permissionDTO
    ) {
        UUID createdPermissionId = permissionService.create(permissionDTO);
        return ApiSuccessResponse.<UUID>builder()
                .data(createdPermissionId)
                .statusCode(HttpStatus.CREATED)
                .build();
    }

    @PatchMapping("/{permissionId}")
    @PreAuthorize("hasAuthority('PERMISSION:UPDATE')")
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

    @DeleteMapping("/{permissionId}")
    @ApiResponse(responseCode = "204")
    @PreAuthorize("hasAuthority('PERMISSION:DELETE')")
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
