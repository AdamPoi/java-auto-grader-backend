package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.model.dto.PermissionDTO;
import io.adampoi.java_auto_grader.model.dto.RoleDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;

    public RoleService(final RoleRepository roleRepository,
                       final PermissionRepository permissionRepository, final UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<RoleDTO> findAll(QueryFilter<Role> filter, Pageable pageable) {
        final Page<Role> page = roleRepository.findAll(filter, pageable);

        Page<RoleDTO> dtoPage = new PageImpl<>(
                page.getContent()
                        .stream()
                        .map(role -> mapToDTO(role, new RoleDTO()))
                        .collect(Collectors.toList()),
                pageable,
                page.getTotalElements());
        return PageResponse.from(dtoPage);
    }

    public RoleDTO get(final UUID roleId) {
        return roleRepository.findById(roleId)
                .map(role -> mapToDTO(role, new RoleDTO()))
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    public RoleDTO create(final RoleDTO roleDTO) {
        final Role role = new Role();
        mapToEntity(roleDTO, role);
        return mapToDTO(roleRepository.save(role), new RoleDTO());
    }

    public RoleDTO update(final UUID roleId, final RoleDTO roleDTO) {
        final Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        mapToEntity(roleDTO, role);
        return mapToDTO(roleRepository.save(role), new RoleDTO());
    }

    public void delete(final UUID roleId) {
        roleRepository.deleteById(roleId);
    }

    private RoleDTO mapToDTO(final Role role, final RoleDTO roleDTO) {
        roleDTO.setId(role.getId());
        roleDTO.setName(role.getName());
        roleDTO.setPermissions(role.getRolePermissions().stream()
                .map(permission -> PermissionService.mapToDTO(permission, new PermissionDTO()))
                .collect(Collectors.toList()));
        return roleDTO;
    }

    private Role mapToEntity(final RoleDTO roleDTO, final Role role) {
        role.setName(roleDTO.getName());
        if (!roleDTO.getPermissions().isEmpty()) {
            role.setRolePermissions(roleDTO.getPermissions().stream()
                    .map(permission -> permissionRepository.findById(permission.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet()));
        }
        return role;
    }

    public ReferencedWarning getReferencedWarning(final UUID roleId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));
        if (!role.getRolesUser().isEmpty()) {
            referencedWarning.setKey("role.user.role.referenced");
            role.getRolesUser().forEach(user -> referencedWarning.addParam(user.getId()));
            return referencedWarning;
        }
        return null;
    }
}
