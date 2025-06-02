package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.domain.Role;
import io.adampoi.java_auto_grader.model.dto.RoleDTO;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.repository.UserRepository;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
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

    public Page<RoleDTO> findAll(final Pageable pageable, Map<String, RoleDTO> params) {
        Specification<Role> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(params.get("name"))) {
                predicates.add(builder.like(root.get("name"), "%" + params.get("name") + "%"));
            }

            return query.where(predicates.toArray(new Predicate[]{})).getRestriction();
        };

        final Page<Role> page = roleRepository.findAll(specification, pageable);
        return new PageImpl<>(page.getContent()
                .stream()
                .map(role -> mapToDTO(role, new RoleDTO()))
                .collect(Collectors.toList()),
                pageable, page.getTotalElements());
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
        roleDTO.setRolePermissionPermissions(role.getRolePermissions().stream()
                .map(permission -> permission.getId())
                .toList());
        return roleDTO;
    }

    private Role mapToEntity(final RoleDTO roleDTO, final Role role) {
        role.setName(roleDTO.getName());
        final List<Permission> rolePermissionPermissions = permissionRepository.findAllById(
                roleDTO.getRolePermissionPermissions() == null ? List.of() : roleDTO.getRolePermissionPermissions());
        if (rolePermissionPermissions.size() != (roleDTO.getRolePermissionPermissions() == null ? 0
                : roleDTO.getRolePermissionPermissions().size())) {
            throw new NotFoundException("one of rolePermissionPermissions not found");
        }
        role.setRolePermissions(new HashSet<>(rolePermissionPermissions));
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
