package io.adampoi.java_auto_grader.service;

import io.adampoi.java_auto_grader.domain.Permission;
import io.adampoi.java_auto_grader.model.dto.PermissionDTO;
import io.adampoi.java_auto_grader.model.response.PageResponse;
import io.adampoi.java_auto_grader.repository.PermissionRepository;
import io.adampoi.java_auto_grader.repository.RoleRepository;
import io.adampoi.java_auto_grader.util.NotFoundException;
import io.adampoi.java_auto_grader.util.ReferencedWarning;
import io.github.acoboh.query.filter.jpa.processor.QueryFilter;
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
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public PermissionService(final PermissionRepository permissionRepository,
                             final RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    public PageResponse<PermissionDTO> findAll(QueryFilter<Permission> filter, Pageable pageable) {
        final Page<Permission> page = permissionRepository.findAll(filter, pageable);

        Page<io.adampoi.java_auto_grader.model.dto.PermissionDTO> dtoPage = new PageImpl<>(
                page.getContent()
                        .stream()
                        .map(permission -> mapToDTO(permission, new io.adampoi.java_auto_grader.model.dto.PermissionDTO()))
                        .collect(Collectors.toList()),
                pageable,
                page.getTotalElements()
        );
        return PageResponse.from(dtoPage);
    }

    public PermissionDTO get(final UUID permissionId) {
        return permissionRepository.findById(permissionId)
                .map(permission -> mapToDTO(permission, new PermissionDTO()))
                .orElseThrow(() -> new NotFoundException("Permission not found"));
    }

    public UUID create(final PermissionDTO permissionDTO) {
        final Permission permission = new Permission();
        mapToEntity(permissionDTO, permission);
        return permissionRepository.save(permission).getId();
    }

    public void update(final UUID permissionId, final PermissionDTO permissionDTO) {
        final Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new NotFoundException("Permission not found"));
        mapToEntity(permissionDTO, permission);
        permissionRepository.save(permission);
    }

    public void delete(final UUID permissionId) {
        permissionRepository.deleteById(permissionId);
    }

    private PermissionDTO mapToDTO(final Permission permission, final PermissionDTO permissionDTO) {
        permissionDTO.setId(permission.getId());
        permissionDTO.setName(permission.getName());
        permissionDTO.setDescription(permission.getDescription());
        return permissionDTO;
    }

    private Permission mapToEntity(final PermissionDTO permissionDTO, final Permission permission) {
        permission.setName(permissionDTO.getName());
        permission.setDescription(permissionDTO.getDescription());
        return permission;
    }

    public ReferencedWarning getReferencedWarning(final UUID permissionId) {
        final ReferencedWarning referencedWarning = new ReferencedWarning();
        final Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new NotFoundException("Permission not found"));
        if (!permission.getPermissionRoles().isEmpty()) {
            referencedWarning.setKey("permission.role.permission.referenced");
            permission.getPermissionRoles().forEach(role -> referencedWarning.addParam(role.getId()));
            return referencedWarning;
        }
        return null;
    }

    private Specification<Permission> buildSpecification(final Map<String, PermissionDTO> params) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (Objects.nonNull(params.get("name"))) {
                predicates.add(cb.like(root.get("name"), "%" + params.get("name").getName() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

}
